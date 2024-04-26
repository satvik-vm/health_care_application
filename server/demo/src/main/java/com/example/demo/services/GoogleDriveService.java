package com.example.demo.services;


import com.example.demo.models.DriveResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.stereotype.Service;
import com.google.api.services.drive.model.Permission;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleDriveService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String SERVICE_ACCOUNT_KEY_PATH = getPathToGoogleCredentials();

    private static String getPathToGoogleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "driveCred.json");
        return filePath.toString();
    }

    public DriveResponse uploadDescriptiveMsgToDrive(File file) throws GeneralSecurityException, IOException{
        DriveResponse res = new DriveResponse();
        try{
            String folderId = "10uKgB16jBi93yxlyU1OOK8qj7vV4MvUn";
            Drive drive = createDriveService();
            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(file.getName());
            fileMetaData.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("audio/wav", file);
            com.google.api.services.drive.model.File uploadedFile = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id").execute();
//            drive.permissions().create(uploadedFile.getId(), new Permission()
//                            .setType("user")
//                            .setRole("reader")
//                            .setEmailAddress(doctorEmail))
//                    .execute();
            String audioUrl = "https://drive.google.com/file/d/"+uploadedFile.getId();
            System.out.println("Audio URL: " + audioUrl);
            file.delete();
            res.setStatus(200);
            res.setMsg("Audio Successfully Uploaded To Drive");
            res.setUrl(audioUrl);

        }catch(Exception e)
        {
            System.out.println(e.getMessage());
            res.setStatus(500);
            res.setMsg(e.getMessage());
        }
        return res;
    }

    public DriveResponse uploadMedicalFileToDrive(File file) throws GeneralSecurityException, IOException {
        DriveResponse res = new DriveResponse();
        try {
            String folderId = "16YCVu3wZpNhJVoH5Gaub1QpESbRs4CAx"; // Replace with your folder ID
            Drive drive = createDriveService();
            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(file.getName());
            fileMetaData.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("application/json", file);
            com.google.api.services.drive.model.File uploadedFile = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id").execute();
//            drive.permissions().create(uploadedFile.getId(), new Permission()
//                            .setType("user")
//                            .setRole("reader")
//                            .setEmailAddress(doctorEmail))
//                    .execute();
            String fileUrl = "https://drive.google.com/file/d/" + uploadedFile.getId();
            System.out.println("File URL: " + fileUrl);
            res.setStatus(200);
            res.setMsg("Medical File Successfully Uploaded To Drive");
            res.setUrl(fileUrl);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            res.setStatus(500);
            res.setMsg(e.getMessage());
        }
        return res;
    }

    public String readJsonFromUrl(String url) throws IOException {
        // Convert the Google Drive sharing URL to a direct download URL
        String fileId = url.substring(url.lastIndexOf('/') + 1);
        String directDownloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

        // Open a connection to the URL
        HttpURLConnection conn = (HttpURLConnection) new URL(directDownloadUrl).openConnection();
        conn.setRequestMethod("GET");

        // Create a temporary file
        Path tempFile = Files.createTempFile("temp", ".json");

        // Write the content of the URL to the temporary file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             BufferedWriter out = Files.newBufferedWriter(tempFile)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                out.write(inputLine);
                out.newLine();
            }
        }

        // Disconnect the connection
        conn.disconnect();

        // Read the JSON data from the temporary file
        String jsonContent = new String(Files.readAllBytes(tempFile));

        // Delete the temporary file
        Files.delete(tempFile);

        // Return the JSON data
        return jsonContent;
    }

    public String replaceFile(String fileId, Path filePath, String mimeType) throws IOException, GeneralSecurityException {
        // Initialize Google Drive service
        Drive driveService = createDriveService();

        // Upload the updated file to Google Drive with the same name
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(fileId); // use the old file ID as the name of the new file
        java.io.File filePathJava = filePath.toFile();
        FileContent mediaContent = new FileContent(mimeType, filePathJava);
        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();

        // Get the URL of the uploaded file
        String uploadedFileId = uploadedFile.getId();
        String uploadedFileUrl = "https://drive.google.com/file/d/" + uploadedFileId;

        // Delete the old file from Google Drive
        driveService.files().delete(fileId).execute();

        return uploadedFileUrl;
    }

    public void removeFile(String fileId) throws IOException, GeneralSecurityException {
        // Initialize Google Drive service
        Drive driveService = createDriveService();

        // Delete the old file from Google Drive
        driveService.files().delete(fileId).execute();
    }

    private Drive createDriveService() throws GeneralSecurityException, IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .build();
    }
}
