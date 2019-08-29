package utils;


import okhttp3.MediaType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    public static String fetchJsonData(String url) throws Exception{
        BufferedReader in = null;
        try {
            URL urlForGetRequest = new URL(url);
            String readLine;
            HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                in.close();
                return response.toString();
            }
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) { }
        }
        return null;
    }

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");   // something

    public static void postFile(File file, String url) throws IOException {
        /*
        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "User Replay")
                .addFormDataPart("replay", file.getName(),
                        RequestBody.create(MEDIA_TYPE_PNG, file))
                .build();

        Request req = new Request.Builder()
                .header("Authorization", "discordbot-w3risk")
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(req).execute();
        System.out.println(response.message());
        */

    }

    /*
    public static void postFile(File file, String url) throws IOException {
        URL urlForPostRequest = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) urlForPostRequest.openConnection();

        //


        String boundaryString = "------Boundary";
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

        OutputStream outputStreamToRequestBody = con.getOutputStream();
        BufferedWriter httpRequestBodyWriter =
                new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

        // Include value from the myFileDescription text area in the post data
        httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
        httpRequestBodyWriter.write("\n\n");
        httpRequestBodyWriter.write("Log file for 20150208");

        // Include the section to describe the file
        httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data;"
                + "name=\"myFile\";"
                + "filename=\""+ file.getName() +"\""
                + "\nContent-Type: w3g\n\n");
        httpRequestBodyWriter.flush();

        // Write the actual file contents
        FileInputStream inputStreamToLogFile = new FileInputStream(file);

        int bytesRead;
        byte[] dataBuffer = new byte[8192];
        while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
            outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
        }

        outputStreamToRequestBody.flush();

        // Mark the end of the multipart http request
        httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
        httpRequestBodyWriter.flush();

        // Close the streams
        outputStreamToRequestBody.close();
        httpRequestBodyWriter.close();

        System.out.println("Response: " + con.getResponseMessage());
    }
    */
    /*
    public static void postFile(File file, String url) throws IOException {
        HttpURLConnection connection = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedOutputStream out = null;
        try {
            URL urlForPostRequest = new URL(url);
            connection = (HttpURLConnection) urlForPostRequest.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            //if (responseCode == HttpURLConnection.HTTP_OK) {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                out = new BufferedOutputStream(connection.getOutputStream());
                byte[] buffer = new byte[8192];
                int i;
                while ((i = bis.read(buffer)) > 0) {
                    out.write(buffer, 0, i);
                }
           //}
            int responseCode = connection.getResponseCode();
            System.out.println("Response: " + connection.getResponseMessage());
            System.out.println(responseCode);
        } finally {
            if (fis != null) fis.close();
            if (bis != null) bis.close();
            if (out != null) out.close();
            System.out.println("closed");
        }
    }
    */
}
