package ma.ensa.compteretrofit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import ma.ensa.compteretrofit.adapters.CompteAdapter;
import ma.ensa.compteretrofit.models.Compte;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private Button button;
    private boolean isXmlFormat = false; // Par défaut, JSON est sélectionné

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btn_add);
        button.setOnClickListener(v -> { addCompte(); });

        // Initialiser RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurer le Spinner
        Spinner spinnerFormat = findViewById(R.id.spinner_format);
        spinnerFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isXmlFormat = (position == 1); // Check if XML is selected
                fetchComptes(); // Charger les données après sélection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Aucun élément sélectionné
            }
        });

        // Charger les données par défaut (avec JSON)
        fetchComptes();
    }

    private void fetchComptes() {
        // Setup the SOAP request for the comptes data
        String NAMESPACE = "http://service.gestioncompte.example.com/";
        String URL = "http://10.0.2.2:8082/services/WS?wsdl";
        String METHOD_NAME = "getAllComptes";
        String SOAP_ACTION = "http://service.gestioncompte.example.com/getAllComptes";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        // Create SoapSerializationEnvelope and set its version
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            // Parse response and set adapter for RecyclerView
            List<Compte> comptes = parseComptes(response); // Method to parse XML response to List of Compte
            recyclerView.setAdapter(new CompteAdapter(comptes, MainActivity.this));
        } catch (Exception e) {
            Log.e(TAG, "Error fetching comptes: " + e.getMessage());
        }
    }

    public void updateCompte(Long id, Compte currentCompte) {
        // Create a dialog to update a compte
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Mettre à jour le compte");

        // Create dialog layout with EditTexts for each field
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_compte, null);
        EditText editTextSolde = dialogView.findViewById(R.id.editTextSolde);
        EditText editTextDateCreation = dialogView.findViewById(R.id.editTextDateCreation);
        EditText editTextType = dialogView.findViewById(R.id.editTextType);

        // Set EditText values with current account values
        editTextSolde.setText(String.valueOf(currentCompte.getSolde()));
        editTextDateCreation.setText(currentCompte.getDateCreation());
        editTextType.setText(currentCompte.getType());

        builder.setView(dialogView)
                .setPositiveButton("Mettre à jour", (dialog, which) -> {
                    double newSolde = Double.parseDouble(editTextSolde.getText().toString());
                    String newDateCreation = editTextDateCreation.getText().toString();
                    String newType = editTextType.getText().toString();

                    Compte updatedCompte = new Compte(id, newSolde, newDateCreation, newType);
                    updateCompteInDatabase(id, updatedCompte);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateCompteInDatabase(Long id, Compte updatedCompte) {
        // Setup the SOAP request for updating a compte
        String NAMESPACE = "http://service.gestioncompte.example.com/";
        String URL = "http://10.0.2.2:8082/services/WS?wsdl";
        String METHOD_NAME = "updateCompte";
        String SOAP_ACTION = "http://service.gestioncompte.example.com/updateCompte";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("id", id);
        request.addProperty("solde", updatedCompte.getSolde());
        request.addProperty("dateCreation", updatedCompte.getDateCreation());
        request.addProperty("type", updatedCompte.getType());

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            // Check response
            SoapObject response = (SoapObject) envelope.getResponse();
            if (response != null) {
                Toast.makeText(MainActivity.this, "Compte mis à jour avec succès", Toast.LENGTH_SHORT).show();
                fetchComptes();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating compte: " + e.getMessage());
        }
    }

    public void deleteCompte(Long id) {
        // Setup SOAP request to delete a compte
        String NAMESPACE = "http://service.gestioncompte.example.com/";
        String URL = "http://10.0.2.2:8082/services/WS?wsdl";
        String METHOD_NAME = "deleteCompte";
        String SOAP_ACTION = "http://service.gestioncompte.example.com/deleteCompte";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("id", id);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            if (response != null) {
                Toast.makeText(MainActivity.this, "Compte supprimé avec succès", Toast.LENGTH_SHORT).show();
                fetchComptes();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting compte: " + e.getMessage());
        }
    }

    private void addCompteToDatabase(Compte newCompte) {
        // Setup SOAP request for adding a new compte
        String NAMESPACE = "http://service.gestioncompte.example.com/";
        String URL = "http://10.0.2.2:8082/services/WS?wsdl";
        String METHOD_NAME = "createCompte";
        String SOAP_ACTION = "http://service.gestioncompte.example.com/createCompte";

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("solde", newCompte.getSolde());
        request.addProperty("dateCreation", newCompte.getDateCreation());
        request.addProperty("type", newCompte.getType());

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            if (response != null) {
                Toast.makeText(MainActivity.this, "Compte ajouté avec succès", Toast.LENGTH_SHORT).show();
                fetchComptes();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding compte: " + e.getMessage());
        }
    }
    public List<Compte> parseComptes(SoapObject response) {
        List<Compte> comptes = new ArrayList<>();

        try {
            // Assuming that the response contains a "Comptes" element which holds the list
            SoapObject comptesList = (SoapObject) response.getProperty("Comptes");
            for (int i = 0; i < comptesList.getPropertyCount(); i++) {
                SoapObject compteObject = (SoapObject) comptesList.getProperty(i);

                // Extracting properties from the XML response
                Long id = Long.parseLong(compteObject.getProperty("id").toString());
                double solde = Double.parseDouble(compteObject.getProperty("solde").toString());
                String dateCreation = compteObject.getProperty("dateCreation").toString();
                String type = compteObject.getProperty("type").toString();

                // Creating a new Compte object
                Compte compte = new Compte(id, solde, dateCreation, type);

                // Adding to the list
                comptes.add(compte);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing comptes: " + e.getMessage());
        }

        return comptes;
    }
    public void addCompte() {
        // Create a dialog to add a new compte
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Ajouter un nouveau compte");

        // Create dialog layout with EditTexts for each field
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);
        EditText editTextSolde = dialogView.findViewById(R.id.editTextSolde);
        EditText editTextDateCreation = dialogView.findViewById(R.id.editTextDateCreation);
        EditText editTextType = dialogView.findViewById(R.id.editTextType);

        builder.setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    double newSolde = Double.parseDouble(editTextSolde.getText().toString());
                    String newDateCreation = editTextDateCreation.getText().toString();
                    String newType = editTextType.getText().toString();

                    Compte newCompte = new Compte(null, newSolde, newDateCreation, newType);
                    addCompteToDatabase(newCompte);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        builder.create().show();



    }

}
