package com.isa0091.utecreportaordenanza;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.isa0091.utecreportaordenanza.ListResources.AdapterReport;
import com.isa0091.utecreportaordenanza.ListResources.ItemReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListadoCompleto extends AppCompatActivity implements AdapterReport.InterfaceReportes {

    String usuario;
    String edificio ;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ItemReport> itemReportList;
    String imagen64b = null;
    String idreport,idestadop;


    private  String UrlReportesEdificio = "http://192.168.1.157:80/UtecReportaOrdenanza/Webservices/ReportesEdificio.php?idEdificio=";
    private String UrlGetImagen = "http://192.168.1.157:80/UtecReporta/Webservices/GetImagen.php?idimagen=";
    private String UrlCambiarEstado="http://192.168.1.157:80/UtecReportaOrdenanza/Webservices/CambiarEstado.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_completo);
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        usuario =  sharedpreferences.getString("iduser", null);//getIntent().getStringExtra("iduser", "Admin");
        edificio=  sharedpreferences.getString("idEdificio", null);

        if (usuario == null) {
            Intent login = new Intent(getBaseContext(), Login.class);
            startActivity(login);
        }


        recyclerView = (RecyclerView) findViewById(R.id.reciclerlista);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CargarDatos();

    }

    private void CargarDatos(){
        itemReportList = new ArrayList<>();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
        String urldata= UrlReportesEdificio + edificio;

        StringRequest urlrequest = new StringRequest(Request.Method.GET, urldata, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("reportesbyedificio");

                    for(int i=0; i< jsonArray.length(); i++){
                        JSONObject o = jsonArray.getJSONObject(i);

                        ItemReport itemReport = new ItemReport(
                                o.getInt("idreporte"),
                                o.getInt("idestado"),
                                o.getString("titulo"),
                                o.getString("descripcion"),
                                o.getString("tipo"),
                                o.getString("imagen"),
                                o.getString("edificio"),
                                o.getString("estado"),
                                o.getString("ordenanza"),
                                o.getString("nombreordenanza"),
                                usuario);

                        itemReportList.add(itemReport);
                    }
                    configurarRecycler(itemReportList);

                } catch (JSONException e) {
                    e.printStackTrace();
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

    private void configurarRecycler(List<ItemReport> itemReportList) {
        adapter = new AdapterReport(itemReportList,getApplicationContext(), ListadoCompleto.this);
        recyclerView.setAdapter(adapter);
    }



    public void cerrarsession(View view) {

        usuario=null;
        SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("iduser");
        editor.remove("idEdificio");
        editor.clear();
        editor.apply();

        Intent Reporte = new Intent(getBaseContext(), Login.class);
        startActivity(Reporte);
        finish();
    }

    @Override
    public void onReporteClickeado(int idresporte) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String cod = String.valueOf(idresporte);
        String Urlimagen = UrlGetImagen +cod;

        StringRequest urlrequest = new StringRequest(Request.Method.GET, Urlimagen, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("imagendata");
                    JSONObject imagendata = jsonArray.getJSONObject(0);
                    imagen64b= imagendata.getString("imagenbase64");
                    MostrarPrevia(imagen64b);

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(),"No posee imagen", Toast.LENGTH_LONG).show();

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

    @Override
    public void CambiarEstado(int idresporte, int idestadoactual) {

        String mensaje="" ;
        idreport=String.valueOf(idresporte);
        idestadop= String.valueOf(idestadoactual);
        switch (idestadoactual){
            case 1:
                mensaje="¿Desea iniciar progreso en este incidente?";
                break;

            case 2:
                mensaje="¿Desea colocar como finalizado este incidente?";
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ListadoCompleto.this);
        builder.setTitle("¿Esta seguro?");

        // Ask the final question
        builder.setMessage(mensaje);

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CambiarEstadopeticion();
            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

      AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
      dialog.show();

      //  AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
      //  builder.setMessage(mensaje).show();

    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    CambiarEstadopeticion();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };
    public void CambiarEstadopeticion(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
        String Urlcambio= UrlCambiarEstado + "idreporte=" + idreport +"&ordenanza="+usuario + "&idestadoactual="+idestadop;

        StringRequest urlrequest = new StringRequest(Request.Method.GET, Urlcambio, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("cambiarestadodata");
                    JSONObject resultado = jsonArray.getJSONObject(0);
                    String estadocambiado=resultado.getString("Resultado");

                    if(estadocambiado.contains("1")){
                        CargarDatos();
                        Toast.makeText(getBaseContext(),"Estado Cambiado Correctamente ", Toast.LENGTH_LONG).show();

                    }else{
                        Toast.makeText(getBaseContext(),"Error al cambiar de estado" , Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(),"Error al cambiar de estado" + e.getMessage() , Toast.LENGTH_LONG).show();

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



    public void MostrarPrevia(String imagen){

        Bitmap imagenbitmap=null;
        ImageView previa;
        if( !imagen.isEmpty()){
            byte [] byteImagen= Base64.decode(imagen,Base64.DEFAULT);
            imagenbitmap= BitmapFactory.decodeByteArray(byteImagen,0,byteImagen.length);
        }

        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.activity_previaimagen, null);
        previa =(ImageView) view.findViewById(R.id.imagenpreview);
        previa.setImageBitmap(imagenbitmap);
        alertadd.setView(view);
        alertadd.show();
    }
}

