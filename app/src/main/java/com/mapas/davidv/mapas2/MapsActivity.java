package com.mapas.davidv.mapas2;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements View.OnClickListener,OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnMapNormal;
    private Button btnMapTerreno;
    private Button btnMapHibrido;
    private Button btnMapSatelite;
    private Button btnInterior;
    private Button btnCalcDistance;
    private List<LatLng> listPosMarc = new ArrayList<>();
    private List<Marker> listMaker = new ArrayList<>();
    private Polyline poliLinea ;
    private EditText edTextCalcDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnMapNormal = (Button)findViewById(R.id.btnMapNormal);
        btnMapTerreno = (Button)findViewById(R.id.btnMapTerreno);
        btnMapHibrido = (Button)findViewById(R.id.btnMapHibrido);
        btnMapSatelite = (Button)findViewById(R.id.btnMapSatelite);
        btnInterior = (Button)findViewById(R.id.btnInterior);
        btnCalcDistance = (Button)findViewById(R.id.btnCalcDistance);
        edTextCalcDistance = (EditText)findViewById(R.id.edTextCalcDistance);

        btnMapNormal.setOnClickListener(this);
        btnMapTerreno.setOnClickListener(this);
        btnMapHibrido.setOnClickListener(this);
        btnMapSatelite.setOnClickListener(this);
        btnInterior.setOnClickListener(this);
        btnCalcDistance.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Cuando hacemos un click largo sobre un punto del mapa se añade un marcador en el mismo
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if (listPosMarc.size() <= 1) {

                    /*mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.place_holder))
                            .anchor(0.0f, 1.0f)
                            .position(latLng));*/
                    Marker m = mMap.addMarker(new MarkerOptions()
                            .position(latLng));

                    m.setDraggable(true);

                    listPosMarc.add(latLng);
                    listMaker.add(m);

                    if (listPosMarc.size() == 2 && poliLinea == null) {
                        setLinea(listPosMarc.get(0), listPosMarc.get(1));
                        calcularDistancia();
                    }
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (listPosMarc.size() > 1) {
                    if (listPosMarc.get(0).equals(marker.getPosition())) {
                        listPosMarc.remove(0);
                    } else {
                        listPosMarc.remove(1);
                    }
                } else {
                    listPosMarc.clear();
                }

                marker.remove();
                listMaker.remove(marker);

                return true;
            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                polyline.remove();
                listPosMarc.clear();
                poliLinea =  null;
                //Borrar los marcadores
                for (Marker m : listMaker) {
                    m.remove();
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng posicion = marker.getPosition();
                

            }

        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnMapNormal :
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.btnMapTerreno :
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.btnMapHibrido :
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.btnMapSatelite :
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.btnInterior :
                // Algunos edificios tienen mapa de interior. Hay que ponerse sobre ellos y directamente veremos las plantas
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(-33.86997, 151.2089), 18));
                break;
            /*case R.id.btnCalcDistance :
                if(listPosMarc.size() == 2){
                    double distancia = obtenerDistancia(listPosMarc.get(0), listPosMarc.get(1));
                    distancia = distancia / 1000 ;
                    edTextCalcDistance.setText(String.valueOf(distancia)+ " km");
                }else {
                    edTextCalcDistance.setText("0.0 km");
                }
                break;*/

        }
    }

    private void calcularDistancia(){

        if(listPosMarc.size() == 2){
            double distancia = obtenerDistancia(listPosMarc.get(0), listPosMarc.get(1));
            distancia = distancia / 1000 ;
            edTextCalcDistance.setText(String.valueOf(distancia)+ " km");
        }else {
            edTextCalcDistance.setText("0.0 km");
        }

    }

    //Devuelve la distancia entre dos puntos en metros
    private double obtenerDistancia(LatLng pos1, LatLng pos2){
        return SphericalUtil.computeDistanceBetween(pos1,pos2);
    }

    //Dibuja una linea entre dos puntos
    private void setLinea(LatLng pos1,LatLng pos2){

        PolylineOptions linea = new PolylineOptions()
                .add(pos1)
                .add(pos2);

        linea.width(2);
        linea.color(Color.BLUE);

        poliLinea = mMap.addPolyline(linea);
        poliLinea.setClickable(true);

    }

}