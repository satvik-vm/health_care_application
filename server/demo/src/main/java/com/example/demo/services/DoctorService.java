package com.example.demo.services;

import com.example.demo.Entity.Doctor;
import com.example.demo.Entity.Hospital;
import com.example.demo.Entity.MedicalRecord;
import com.example.demo.Entity.Patient;
import com.example.demo.Repository.DoctorRepository;
import com.example.demo.Repository.HospitalRepository;
import com.example.demo.Repository.MedicalRecordRepository;
import com.example.demo.Repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoctorService {
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    HospitalRepository hospitalRepository;
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    MedicalRecordRepository medicalRecordRepository;
    @Autowired
    GeneralService generalService;
    @Autowired
    GoogleDriveService googleDriveService;
    public Doctor allocateDoctor(Hospital hospital)
    {
        List<Doctor> doctors = doctorRepository.findByHospitalId(hospital.getId());
        int mini = Integer.MAX_VALUE;
        Doctor allocatedDoctor = null;
        for(Doctor doctor : doctors)
        {
            int patientCount = patientRepository.countByDoctorId(doctor.getId());
            if(mini > patientCount)
            {
                mini = patientCount;
                allocatedDoctor = doctor;
            }
        }
        return allocatedDoctor;
    }

    public List<Patient> viewPatients(String id) {
        List<Patient> patients = patientRepository.findByDoctorId(id);

        Collections.sort(patients, new Comparator<Patient>() {
            @Override
            public int compare(Patient p1, Patient p2) {
                // Sort by health status
                int statusCompare = p2.getHealthStatus().compareTo(p1.getHealthStatus());
                if (statusCompare != 0) {
                    return statusCompare;
                }

                // If health status is the same, sort by most recent visit
                return p1.getMostRecentVisit().compareTo(p2.getMostRecentVisit());
            }
        });

        return patients;
    }


    public String givePrescription(String pid, String prescription) throws IOException, GeneralSecurityException {
        MedicalRecord mr = medicalRecordRepository.findById(pid).get();
        Doctor doctor = mr.getDoctor();
        Patient patient = mr.getPatient();
        String url = mr.getRecord();
        url = generalService.decrypt(url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parse the JSON content into a Java object
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> existingJson = mapper.readValue(response.toString(), Map.class);

        // Create a new JSON object to add
        Map<String, Object> newJson = new HashMap<>();
        newJson.put("timestamp", LocalDateTime.now().toString());
        newJson.put("type", "Prescription");
        newJson.put("doctor", doctor.getUser().getFirstName()); // replace with appropriate method to get doctor's name
        newJson.put("prescription", prescription);

        // Add the new JSON object to the existing JSON
        existingJson.putAll(newJson);

        // Convert the updated JSON back to a string
        String updatedJsonContent = mapper.writeValueAsString(existingJson);

        // Write the updated JSON content to a temporary file
        String fileId = url.substring(url.lastIndexOf('/') + 1);
        Path tempFilePath = Paths.get("temp.json");
        Files.write(tempFilePath, updatedJsonContent.getBytes());

        // Replace the old file on Google Drive with the updated file
        String uploadedFileUrl = googleDriveService.replaceFile(fileId, tempFilePath, "application/json");

        // Update the record in the database
        mr.setRecord(generalService.encrypt(uploadedFileUrl));
        medicalRecordRepository.save(mr);

        // Delete the temporary file
        Files.delete(tempFilePath);

        return "Prescription given successfully!";
    }
}
