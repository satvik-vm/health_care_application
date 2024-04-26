package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.dto.PatientDTO;
import com.example.demo.models.DriveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
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
    @Autowired
    IdMappingRepository idMappingRepository;
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

    public List<PatientDTO> viewPatients(String email) {
        List<Patient> patients = patientRepository.findByDoctor_User_Email(email);

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
        List<PatientDTO> patientDTOs = new ArrayList<>();
        for (Patient patient : patients) {
            PatientDTO patientDTO = new PatientDTO();
            patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId());
            patientDTO.setAabhaId(patient.getAabhaId());
            patientDTO.setFirstName(patient.getUser().getFirstName());
            patientDTO.setLastName(patient.getUser().getLastName());
            patientDTO.setStatus(patient.getHealthStatus());
            patientDTOs.add(patientDTO);
        }

        return patientDTOs;
    }


    public String givePrescription(int id, String prescription) throws IOException, GeneralSecurityException {
        Optional<IdMapping> idMappingOptional = idMappingRepository.findById(id);
        if (!idMappingOptional.isPresent()) {
            throw new IllegalArgumentException("No IdMapping found with id: " + id);
        }
        String pid = idMappingOptional.get().getPrivateId().toString();

        MedicalRecord mr = medicalRecordRepository.findByPatient_Id(pid);
        Doctor doctor = mr.getDoctor();
        Patient patient = mr.getPatient();
        String url = mr.getRecord();
        url = generalService.decrypt(url);

        // Read the existing JSON content from the Google Drive file
        String existingJsonContent = googleDriveService.readJsonFromUrl(url);

        // Parse the existing JSON content into a Java object
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> existingJson = mapper.readValue(existingJsonContent, new TypeReference<List<Map<String, Object>>>(){});

        // Create a new JSON object for the prescription
        Map<String, Object> newJson = new HashMap<>();
        newJson.put("timestamp", LocalDateTime.now().toString());
        newJson.put("type", "Prescription");
        newJson.put("doctor", doctor.getUser().getFirstName()); // replace with appropriate method to get doctor's name
        newJson.put("prescription", prescription);

        // Add the new JSON object to the list of existing JSON objects
        existingJson.add(newJson);

        // Convert the updated list of JSON objects back to a JSON string
        String updatedJsonContent = mapper.writeValueAsString(existingJson);

        // Write the updated JSON content to a temporary file
        Path tempFilePath = Files.createTempFile(generalService.encrypt(patient.getUser().getEmail()), ".json");
        Files.write(tempFilePath, updatedJsonContent.getBytes());

        // Remove the old file from Google Drive
        String fileId = url.substring(url.lastIndexOf('/') + 1);
        googleDriveService.removeFile(fileId);

        // Upload the new file to Google Drive
        File tempFile = tempFilePath.toFile();
        DriveResponse driveResponse = googleDriveService.uploadMedicalFileToDrive(tempFile);
        System.out.println(driveResponse.getUrl());

        // Update the record in the database
        String uploadedFileUrl = driveResponse.getUrl();
        mr.setRecord(generalService.encrypt(uploadedFileUrl));
        medicalRecordRepository.save(mr);

        // Delete the temporary file
        Files.delete(tempFilePath);

        return "Prescription given successfully!";
    }

    public String seeReport(int id, String email) throws IOException {
        String patientId = idMappingRepository.findById(id).get().getPrivateId().toString();
        MedicalRecord mr = medicalRecordRepository.findByPatient_Id(patientId);
        Patient patient = patientRepository.findById(patientId).get();

        if(patient.getDoctor().getUser().getEmail().equals(email))
        {
            String url = generalService.decrypt(mr.getRecord());
            System.out.println(url);
            return googleDriveService.readJsonFromUrl(url);
        }
        else
        {
            return "You are not authorized to view this report";
        }
    }

    public String getDocNameByEmail(String email) {
        return doctorRepository.findByUser_Email(email).getUser().getFirstName();
    }

    public List<PatientDTO> viewActivePatients(String email, List<String> statusList) {

        List<Patient> patientList = patientRepository.findByDoctor_User_EmailAndHealthStatusIn(email, statusList);
        List<PatientDTO> patientDTOs = new ArrayList<>();
        for (Patient patient : patientList) {
            PatientDTO patientDTO = new PatientDTO();
            patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId());
            patientDTO.setAabhaId(patient.getAabhaId());
            patientDTO.setFirstName(patient.getUser().getFirstName());
            patientDTO.setLastName(patient.getUser().getLastName());
            patientDTO.setStatus(patient.getHealthStatus());
            patientDTOs.add(patientDTO);
        }

        return patientDTOs;
    }


    public Boolean updatePatientStatus(int publicId) {

        try{
            // Map the public ID to the private ID
            String privateId = idMappingRepository.findById(publicId).get().getPrivateId().toString();

            // Retrieve the patient entity using the private ID
            Patient patient = patientRepository.findById(privateId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            // Update the status to "GREEN"
            patient.setHealthStatus("GREEN");

            // Save the updated patient entity
            patientRepository.save(patient);

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
