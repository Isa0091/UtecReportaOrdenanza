package com.isa0091.utecreportaordenanza;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Menu extends AppCompatActivity {

    String usuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        usuario =  sharedpreferences.getString("iduser", null);//getIntent().getStringExtra("iduser", "Admin");

        if (usuario == null) {
            Intent login = new Intent(getBaseContext(), Login.class);
            startActivity(login);
        }
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


    public void ListadoGeneral(View view){

        Intent Listado = new Intent(getBaseContext(), ListadoCompleto.class);
        startActivity(Listado);
    }

    public void listadoDoordenanza(View view){

        Intent Reporte = new Intent(getBaseContext(), ListadoDordenanza.class);
        startActivity(Reporte);
    }

}
