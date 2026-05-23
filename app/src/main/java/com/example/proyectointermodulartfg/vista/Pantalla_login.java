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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class Pantalla_login extends AppCompatActivity {
    private EditText etCorreo, etClave;
    private TextView tvRecuperarClave, tvRegistrarse;
    private Button btnLogin;
    private MaterialButton btnGoogleLogin;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;
    private final String WEB_CLIENT_ID = "491560371485-56c1ir2utkud03vmord2ak6qb2mhc8jt.apps.googleusercontent.com";
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

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
                        loginGoogle(task);
                    }
                }
        );

        tvRecuperarClave.setOnClickListener(v -> recuperarClave());
        tvRegistrarse.setOnClickListener(v -> registrarse());

        btnLogin.setOnClickListener(v ->
            executorService.execute(() -> {
                String correo = etCorreo.getText().toString().trim();
                boolean validador = SupabaseHelper.estructuraCorreoValida(correo);
                runOnUiThread(() -> {
                    if (validador) login();
                    else Toast.makeText(Pantalla_login.this, "El correo tiene un formato inválido", Toast.LENGTH_SHORT).show();
                });
            })
        );

        btnGoogleLogin.setOnClickListener(v -> lanzarLoginGoogle());
    }

    private void lanzarLoginGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });
    }

    private void loginGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            String correo = account.getEmail();
            String nombreGoogle = account.getDisplayName();

            executorService.execute(() -> {
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

                    int rol = SupabaseHelper.obtenerRolUsuario(correo);
                    long idUsuarioTemp = -1;
                    try {
                        String jsonUser = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
                        idUsuarioTemp = new org.json.JSONArray(jsonUser).getJSONObject(0).getLong("id");
                    } catch (Exception e) {
                        Log.e("LOGIN_GOOGLE", "Error obteniendo ID: " + e.getMessage());
                    }

                    final long idUsuarioFinal = idUsuarioTemp;

                    runOnUiThread(() -> {
                        if (idUsuarioFinal == -1) {
                            Toast.makeText(this, "Error: No se pudo verificar tu usuario en la base de datos", Toast.LENGTH_LONG).show();
                            return;
                        }
                        confirmacionLoginRealizado(correo, rol, idUsuarioFinal);

                        Intent intent;
                        if (rol == 1) {
                            intent = new Intent(Pantalla_login.this, Pantalla_panel_administrador.class);
                        } else {
                            intent = new Intent(Pantalla_login.this, Pantalla_principal.class);
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error en Supabase Auth", Toast.LENGTH_SHORT).show());
                }
            });

        } catch (ApiException e) {
            Log.e("GoogleAuth", "Error: " + e.getStatusCode());
            Toast.makeText(this, "Fallo inicio sesión: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        String correo = etCorreo.getText().toString().trim();
        String clave = etClave.getText().toString().trim();

        if(correo.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Usuario usuario = new Usuario(correo, clave);
            boolean existe = SupabaseHelper.logearUsuario(usuario);

            if (existe) {
                int rol = SupabaseHelper.obtenerRolUsuario(correo);
                long idUsuarioTemp = -1;
                try {
                    String jsonUser = SupabaseHelper.obtenerDatosTablas("Usuarios", "correo", correo);
                    idUsuarioTemp = new org.json.JSONArray(jsonUser).getJSONObject(0).getLong("id");
                } catch (Exception e) {
                    Log.e("LOGIN", "Error obteniendo ID: " + e.getMessage());
                }

                final long idUsuarioFinal = idUsuarioTemp;

                runOnUiThread(() -> {
                    confirmacionLoginRealizado(correo, rol, idUsuarioFinal);
                    Intent intent;
                    if (rol == 1) {
                        intent = new Intent(Pantalla_login.this, Pantalla_panel_administrador.class);
                    } else {
                        intent = new Intent(Pantalla_login.this, Pantalla_principal.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "El usuario o contraseña son incorrectos", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void recuperarClave() {
        String correo = etCorreo.getText().toString().trim();
        Intent intent = new Intent(this, Pantalla_recuperar_contrasena.class);
        intent.putExtra("correo", correo);
        startActivity(intent);
    }

    private void registrarse() {
        String correo = etCorreo.getText().toString().trim();
        Intent intent = new Intent(this, Pantalla_registrarse.class);
        intent.putExtra("correo", correo);
        startActivity(intent);
    }

    private void confirmacionLoginRealizado(String correo, int rol, long id) {
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("correo_usuario", correo);
        editor.putInt("rol_usuario", rol);
        editor.putLong("id_usuario", id);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}