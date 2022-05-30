package ro.pub.cs.systems.eim.practicaltest02.network;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilites;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;
    Date date = new Date();

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG,  "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilites.getReader(socket);
            PrintWriter printWriter = Utilites.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String informationType = bufferedReader.readLine();

            if (informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }

            BitcoinPriceModel data = serverThread.getData();
            String bitcoinPrice = null;

            if (informationType.equals("USD") && data != null && data.getUsdRate() != null && date.getTime() - data.getUsdTime() < 5000) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                bitcoinPrice = data.getUsdRate();
            } else if (informationType.equals("EUR") && data != null && data.getEurRate() != null && date.getTime() - data.getEurTime() < 5000) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                bitcoinPrice = data.getEurRate();
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");

                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + informationType + ".json");
                Log.e(Constants.TAG, Constants.WEB_SERVICE_ADDRESS + informationType + ".json");

                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else {
                    Log.i(Constants.TAG, pageSourceCode);
                }

                JSONObject content = new JSONObject(pageSourceCode);
                JSONObject bpi = content.getJSONObject("bpi");
                JSONObject currency = bpi.getJSONObject(informationType);
                bitcoinPrice = currency.getString("rate");
                Log.i(Constants.TAG, "[COMMUNICATION THREAD]" + bitcoinPrice);

                BitcoinPriceModel bitcoinPriceModel = null;
                if (informationType.equals("USD")) {
                    bitcoinPriceModel = new BitcoinPriceModel(bitcoinPrice, null);
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Am pus USD");
                } else {
                    bitcoinPriceModel = new BitcoinPriceModel(null, bitcoinPrice);
                }

                serverThread.setData(bitcoinPriceModel);
            }
            BitcoinPriceModel bitcoinPriceModel = serverThread.getData();
            if (bitcoinPriceModel == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result = null;
            if (informationType.equals("USD")) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD]" + informationType);
                result = bitcoinPriceModel.getUsdRate();
                Log.e(Constants.TAG, "[COMMUNICATION THREAD]" + result);
            } else {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD]" + informationType);
                result = bitcoinPriceModel.getEurRate();
                Log.e(Constants.TAG, "[COMMUNICATION THREAD]" + result);
            }

            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
