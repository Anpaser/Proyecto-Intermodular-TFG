package com.example.proyectointermodulartfg.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectointermodulartfg.R;
import com.example.proyectointermodulartfg.controlador.SupabaseHelper;
import com.example.proyectointermodulartfg.modelo.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

@SuppressWarnings("deprecation")
public class Pantalla_login extends AppCompatActivity {
    private EditText etCorreo, etClave;
    private TextView tvRecuperarClave, tvRegistrarse;
    private Button btnLogin;
    private MaterialButton btnGoogleLogin;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;
    private final String WEB_CLIENT_ID = "491560371485-56c1ir2utkud03vmord2ak6qb2mhc8jt.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_login);

        etCorreo = findViewById(R.id.etEmail);
        etClave = findViewById(R.id.etPassword);
        tvRecuperarClave = findViewById(R.id.tvOlvidoClave);
        tvRegistrarse = findViewById(R.id.tvRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );

        tvRecuperarClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarClave();
            }
        });

        tvRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarse();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    String correo = etCorreo.getText().toString().trim();
                    boolean validador = SupabaseHelper.estructuraCorreoValida(correo);
                    runOnUiThread(() -> {
                        if (validador) login(); else Toast.makeText(Pantalla_login.this, "El correo tiene un formato invalido", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarLoginGoogle();
            }
        });
    }

    private void lanzarLoginGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            String correo = account.getEmail();
            String nombreGoogle = account.getDisplayName();

            new Thread(() -> {
                boolean exitoAuth = SupabaseHelper.logearConGoogle(idToken);

                if (exitoAuth) {
                    boolean existeEnTabla = SupabaseHelper.existeUsuario(correo);

                    if (!existeEnTabla) {
                        Usuario nuevo = new Usuario();
                        nuevo.setCorreo(correo);
                        nuevo.setNombre(nombreGoogle != null ? nombreGoogle : "Usuario de Google");
                        nuevo.setClave("OAUTH_USER");

                        SupabaseHelper.registrarUsuario(nuevo);
                    }

                    runOnUiThread(() -> {
                        confirmacionLoginRealizado(correo);
                        Intent intent = new Intent(Pantalla_login.this, Pantalla_principal.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error en Supabase Auth", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (ApiException e) {
            Log.e("GoogleAuth", "Error: " + e.getStatusCode());
            Toast.makeText(this, "Fallo inicio sesión: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void recuperarClave() {
        String correo = etCorreo.getText().toString().trim();
        Intent intent = new Intent(Pantalla_login.this, Pantalla_recuperar_contrasena.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("correo", correo);
        startActivity(intent);
    }

    private void registrarse() {
        String correo = etCorreo.getText().toString().trim();
        Intent intent = new Intent(Pantalla_login.this, Pantalla_registrarse.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("correo", correo);
        startActivity(intent);
    }

    private void login() {
        String correo = etCorreo.getText().toString().trim();
        String clave = etClave.getText().toString().trim();

        if(correo.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        } else {
            new Thread(() -> {
                Usuario usuario = new Usuario(correo, clave);
                boolean existe = SupabaseHelper.logearUsuario(usuario);
                runOnUiThread(() -> {
                    if (existe) {
                        confirmacionLoginRealizado(correo);
                        Intent intent = new Intent(Pantalla_login.this, Pantalla_principal.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "El usuario o contraseña son incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }

    private void confirmacionLoginRealizado(String correo) {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("correo_usuario", correo);
        editor.apply();
    }
}