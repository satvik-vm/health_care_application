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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
            FileContent mediaContent = new FileContent("application/json", file); // Assuming medical files are PDFs
            com.google.api.services.drive.model.File uploadedFile = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id").execute();
//            drive.permissions().create(uploadedFile.getId(), new Permission()
//                            .setType("user")
//                            .setRole("reader")
//                            .setEmailAddress(doctorEmail))
//                    .execute();
            String fileUrl = "https://drive.google.com/file/d/" + uploadedFile.getId();
            System.out.println("File URL: " + fileUrl);
            file.delete();
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
