package ro.pub.cs.systems.eim.practicaltest02;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.network.ClientThread;
import ro.pub.cs.systems.eim.practicaltest02.network.ServerThread;

public class PracticalTest02MainActivity extends AppCompatActivity {

    ServerThread serverThread;
    ClientThread clientThread;

    EditText serverPortText;
    Button serverConnectButton;

    EditText clientAddressText;
    EditText clientPortText;
    Button clientGetValueButton;
    Spinner clientInformationSpinner;
    EditText clientDisplayedValue;

    private ConnectButtonClickListener connectButtonClickListener = new ConnectButtonClickListener();
    private class ConnectButtonClickListener implements Button.OnClickListener {
        public void onClick(View view) {
            String serverPort = serverPortText.getText().toString();
            if (serverPort == null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        }
    }

    private GetValueButtonClickListener getValueButtonClickListener = new GetValueButtonClickListener();
    private class GetValueButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            String clientAddress = clientAddressText.getText().toString();
            String clientPort = clientPortText.getText().toString();
            if (clientAddress == null || clientAddress.isEmpty()
                    || clientPort == null || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }
            String informationType = clientInformationSpinner.getSelectedItem().toString();
            if (informationType == null || informationType.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            clientDisplayedValue.setText("");

            clientThread = new ClientThread(
                    clientAddress, Integer.parseInt(clientPort), informationType, clientDisplayedValue);
            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        serverPortText = (EditText) findViewById(R.id.server_port_edit_text);

        serverConnectButton = (Button) findViewById(R.id.server_connect_button);
        serverConnectButton.setOnClickListener(connectButtonClickListener);


        clientAddressText = (EditText) findViewById(R.id.client_address_edit_text);
        clientPortText = (EditText) findViewById(R.id.client_port_edit_text);
        clientInformationSpinner = (Spinner) findViewById(R.id.client_information_type_spinner);
        clientDisplayedValue = (EditText) findViewById(R.id.client_displayed_value);

        clientGetValueButton = (Button) findViewById(R.id.client_get_value);
        clientGetValueButton.setOnClickListener(getValueButtonClickListener);

    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}