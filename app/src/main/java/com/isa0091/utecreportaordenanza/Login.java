package com.isa0091.utecreportaordenanza;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private EditText correo, pass;
    private Button ingresar1;
    private String userOrdenanza,idedificio,correo_,pass_;
    public static final String MyPREFERENCES = "sesiones" ;
    Context ctx = this;
    public String UrlAutenticar= "http://192.168.1.157:80/UtecReportaOrdenanza/Webservices/AutenticarOrdenanza.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);


        correo = (EditText) findViewById(R.id.usuario);
        correo.requestFocus();
        pass = (EditText) findViewById(R.id.pass);
        ingresar1 = (Button) findViewById(R.id.ingresar11);

        ingresar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetDisponible()){
                    correo_ = correo.getText().toString();
                    pass_ = pass.getText().toString();

                    if (correo.getText().toString().isEmpty()) {

                        Toast.makeText(getBaseContext(), "ingrese un usuario", Toast.LENGTH_SHORT).show();


                    } else if (pass.getText().toString().isEmpty()) {

                        Toast.makeText(getBaseContext(), "Debe ingresar la contrasena", Toast.LENGTH_SHORT).show();
                    }else{
                      AutenticarOrdenanza(correo_,pass_);
                    }

                }else{

                    Toast.makeText(ctx,"Verifica tu Acceso a Internet ",Toast.LENGTH_LONG).show();
                }



            }
        });

    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }


    public void AutenticarOrdenanza(String usuario , String pass) {

        String urlaut=UrlAutenticar + "iduserordenanza="+usuario +"&pass="+pass;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        StringRequest urlrequest = new StringRequest(Request.Method.GET, urlaut, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("datosOrdenanza");
                    JSONObject imagendata = jsonArray.getJSONObject(0);
                    userOrdenanza=imagendata.getString("iduserordenanza");
                    idedificio=imagendata.getString("idedificio");

                    if(userOrdenanza.isEmpty() || userOrdenanza==null){
                        Toast.makeText(getBaseContext(),"Erorr al autenticar usuario, credenciales invalidas", Toast.LENGTH_LONG).show();

                    }else{
                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("iduser",userOrdenanza);
                        editor.putString("idEdificio",idedificio);
                        editor.commit();

                        Intent menu = new Intent(getBaseContext(), Menu.class);
                        startActivity(menu);
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(),"Erorr al autenticar usuario, credenciales invalidas", Toast.LENGTH_LONG).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(urlrequest);

    }
}

