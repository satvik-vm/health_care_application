package com.example.demo.services;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.dto.DoctorQuestionDTO;
import com.example.demo.dto.PatientDTO;
import com.example.demo.models.DriveResponse;
import com.example.demo.models.FollowUpRequest;
import com.example.demo.models.PrescriptionRequest;
import com.example.demo.models.TaskCreationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    AdminService adminService;
    @Autowired
    QuestionnaireRepository questionnaireRepository;
    @Autowired
    SocketIOServer server;
    @Autowired
    TaskRepository taskRepository;
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
            patientDTO.setDistrict(patient.getDistrict());
            patientDTOs.add(patientDTO);
        }

        return patientDTOs;
    }


    public String giveFollowUp(FollowUpRequest request, String doc_email) throws IOException, GeneralSecurityException {
        int id = request.getId();
        Optional<IdMapping> idMappingOptional = idMappingRepository.findById(id);
        if (!idMappingOptional.isPresent()) {
            throw new IllegalArgumentException("No IdMapping found with id: " + id);
        }
        String pid = idMappingOptional.get().getPrivateId().toString();

        MedicalRecord mr = medicalRecordRepository.findByPatient_Id(pid);
        Doctor doctor = mr.getDoctor();
        Patient patient = mr.getPatient();
        String url = mr.getRecord();
        if(doctor.getUser().getEmail().equals(doc_email))
            url = generalService.decrypt(url);
        else
            return "You are not authorized to give followUp to this patient";

        // Read the existing JSON content from the Google Drive file
        String existingJsonContent = googleDriveService.readJsonFromUrl(url);

        // Parse the existing JSON content into a Java object
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> existingJson = mapper.readValue(existingJsonContent, new TypeReference<List<Map<String, Object>>>(){});

        if(request.getType().equals("prescription"))
        {
            PrescriptionRequest prescriptionRequest = request.getPrescription();

            Map<String, Object> prescriptionMap = new HashMap<>();
            prescriptionMap.put("medicine", prescriptionRequest.getMedicine());
            prescriptionMap.put("test", prescriptionRequest.getTest());
            prescriptionMap.put("precaution", prescriptionRequest.getPrecaution());
            prescriptionMap.put("days", prescriptionRequest.getDays());

            Map<String, Object> newJson = new HashMap<>();
            newJson.put("timestamp", request.getTimestamp());
            newJson.put("type", request.getType());
            newJson.put("doctor", doctor.getUser().getFirstName());
            newJson.put("prescription", prescriptionMap);

            // Add the new JSON object to the list of existing JSON objects
            existingJson.add(newJson);
            TaskCreationRequest taskCreationRequest = new TaskCreationRequest();
            taskCreationRequest.setTask_type("prescription");
            taskCreationRequest.setDescription("Prescription for " + patient.getUser().getFirstName() + " " + patient.getUser().getLastName() + " has been uploaded");
            taskCreationRequest.setPId(id);
            createTask(taskCreationRequest);
        }
        else if(request.getType().equals("appointment"))
        {
            // Create a new JSON object for the follow-up
            Map<String, Object> newJson = new HashMap<>();
            newJson.put("timestamp", request.getTimestamp());
            newJson.put("type", request.getType());
            newJson.put("doctor", doctor.getUser().getFirstName()); // replace with appropriate method to get doctor's name
            newJson.put("appointment", "Date: " + request.getAppointment().getDate() + " Time: " + request.getAppointment().getTime() + " Duration: " + request.getAppointment().getDuration());

            // Add the new JSON object to the list of existing JSON objects
            existingJson.add(newJson);
            TaskCreationRequest taskCreationRequest1 = new TaskCreationRequest();
            taskCreationRequest1.setTask_type("appointment_for_doctor");
            taskCreationRequest1.setDescription("Appointment for " + patient.getUser().getFirstName() + " " + patient.getUser().getLastName() + " has been scheduled");
            taskCreationRequest1.setPId(id);
            taskCreationRequest1.setDate(request.getAppointment().getDate());
            taskCreationRequest1.setTime(request.getAppointment().getTime());
            taskCreationRequest1.setDuration(request.getAppointment().getDuration());
            createTask(taskCreationRequest1);
            TaskCreationRequest taskCreationRequest2 = new TaskCreationRequest();
            taskCreationRequest2.setTask_type("appointment_for_field_worker");
            taskCreationRequest2.setDescription("Appointment for " + patient.getUser().getFirstName() + " " + patient.getUser().getLastName() + " has been scheduled");
            taskCreationRequest2.setPId(id);
            taskCreationRequest2.setDate(request.getAppointment().getDate());
            taskCreationRequest2.setTime(request.getAppointment().getTime());
            taskCreationRequest2.setDuration(request.getAppointment().getDuration());
            createTask(taskCreationRequest2);

        }
        else if(request.getType().equals("questionnaire"))
        {
            // Create a new JSON object for the follow-up
            Map<String, Object> newJson = new HashMap<>();
            newJson.put("timestamp", request.getTimestamp());
            newJson.put("type", request.getType());
            newJson.put("doctor", doctor.getUser().getFirstName()); // replace with appropriate method to get doctor's name

            // Convert each DoctorQuestionDTO object to a Map and add it to a list
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> doctorQuestionList = new ArrayList<>();
            for (DoctorQuestionDTO doctorQuestion : request.getDoctorQuestions()) {
                Map<String, Object> doctorQuestionMap = objectMapper.convertValue(doctorQuestion, new TypeReference<Map<String, Object>>() {});
                doctorQuestionList.add(doctorQuestionMap);
            }

            newJson.put("doctorQuestionnaire", doctorQuestionList);

            // Add the new JSON object to the list of existing JSON objects
            existingJson.add(newJson);
            
            TaskCreationRequest taskCreationRequest = new TaskCreationRequest();
            taskCreationRequest.setTask_type("questionnaire");
            taskCreationRequest.setDescription("Questionnaire for " + patient.getUser().getFirstName() + " " + patient.getUser().getLastName() + " has been uploaded");
            taskCreationRequest.setPId(id);
            createTask(taskCreationRequest);
        }
        else if(request.getType().equals("changeStatus"))
        {
            // Create a new JSON object for the follow-up
            Map<String, Object> newJson = new HashMap<>();
            newJson.put("timestamp", request.getTimestamp());
            newJson.put("type", request.getType());
            newJson.put("doctor", doctor.getUser().getFirstName()); // replace with appropriate method to get doctor's name
            newJson.put("status", request.getStatus());
            patient.setHealthStatus(request.getStatus());
            patientRepository.save(patient);
            existingJson.add(newJson);
        }


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
            patientDTO.setDistrict(patient.getDistrict());
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

    public List<PatientDTO> getNewPatients(String email)
    {
        List<Patient> patients = patientRepository.findByDoctor_User_Email(email);
        List<PatientDTO> patientDTOs = new ArrayList<>();
        for (Patient patient : patients) {
            if(patient.getMostRecentVisit() == null)
            {
                PatientDTO patientDTO = new PatientDTO();
                patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId());
                patientDTO.setAabhaId(patient.getAabhaId());
                patientDTO.setFirstName(patient.getUser().getFirstName());
                patientDTO.setLastName(patient.getUser().getLastName());
                patientDTO.setStatus(patient.getHealthStatus());
                patientDTOs.add(patientDTO);
            }
        }
        return patientDTOs;
    }
    public void createTask(TaskCreationRequest request)
    {
        Task task = new Task();
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(task.getId()));
        idMappingRepository.save(idMapping);
        String task_type = request.getTask_type();
        task.setTask_type(task_type);
        Optional<Patient> optionalPatient = patientRepository.findById(idMappingRepository.findById(request.getPId()).get().getPrivateId().toString());
        if(optionalPatient.isPresent())
        {
            if(task_type.equals("prescription")) {
                task.setDescription(request.getDescription());
                LocalDateTime timestamp = LocalDateTime.now().plusDays(2);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String date = timestamp.format(dateFormatter);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String time = timestamp.format(timeFormatter);
                task.setTimestamp(timestamp);
                task.setDate(date);
                task.setTime(time);
                task.setAssignedTime(LocalDateTime.now());
                task.setPatient(optionalPatient.get());
                task.setFieldWorker(optionalPatient.get().getFieldWorker());
                task.setStatus(false);
                Collection<SocketIOClient> allClients = server.getAllClients();
                for (SocketIOClient client : allClients) {
                    String email = client.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
                    if (email.equals(optionalPatient.get().getFieldWorker().getUser().getEmail())) {
                        client.sendEvent("receive_notification", task);
                    }
                }
                taskRepository.save(task);
            }
            else if(task_type.equals("appointment_for_doctor"))
            {
                task.setDescription(request.getDescription());
                task.setAssignedTime(LocalDateTime.now());
                task.setPatient(optionalPatient.get());
                task.setDoctor(optionalPatient.get().getDoctor());
                task.setStatus(false);
                task.setDuration(request.getDuration());
                String date = request.getDate(); // assuming format is "yyyy-MM-dd"
                String time = request.getTime(); // assuming format is "HH:mm"

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);

                task.setTimestamp(dateTime);
                task.setDate(date);
                task.setTime(time);
                taskRepository.save(task);
            }
            else if(task_type.equals("appointment_for_field_worker"))
            {
                task.setDescription(request.getDescription());
                task.setAssignedTime(LocalDateTime.now());
                task.setPatient(optionalPatient.get());
                task.setFieldWorker(optionalPatient.get().getFieldWorker());
                task.setStatus(false);
                task.setDuration(request.getDuration());
                task.setTimestamp(LocalDateTime.now().plusDays(2));
                task.setDate(request.getDate());
                task.setTime(request.getTime());
                Collection<SocketIOClient> allClients = server.getAllClients();
                for (SocketIOClient client : allClients) {
                    String email = client.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
                    if (email.equals(optionalPatient.get().getFieldWorker().getUser().getEmail())) {
                        client.sendEvent("receive_notification", task);
                    }
                }
                taskRepository.save(task);
            }
            else if(task_type.equals("questionnaire"))
            {
                task.setDescription(request.getDescription());
                task.setAssignedTime(LocalDateTime.now());
                task.setPatient(optionalPatient.get());
                task.setStatus(false);
                task.setFieldWorker(optionalPatient.get().getFieldWorker());
                task.setDuration(request.getDuration());
                LocalDateTime timestamp = LocalDateTime.now().plusDays(2);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String date = timestamp.format(dateFormatter);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String time = timestamp.format(timeFormatter);
                task.setTimestamp(timestamp);
                task.setDate(date);
                task.setTime(time);
                Collection<SocketIOClient> allClients = server.getAllClients();
                for (SocketIOClient client : allClients) {
                    String email = client.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
                    if (email.equals(optionalPatient.get().getFieldWorker().getUser().getEmail())) {
                        client.sendEvent("receive_notification", task);
                    }
                }
                taskRepository.save(task);
            }
            else
            {
                throw new IllegalArgumentException("Invalid task type: " + task_type);
            }
        }
        else
        {
            throw new IllegalArgumentException("No patient found with id: " + request.getPId());
        }
    }

    public List<PatientDTO> viewVisitedPatients(String email, List<String> statusList) {
        List<Patient> patientList = patientRepository.findByDoctor_User_EmailAndHealthStatusIn(email, statusList);
        List<PatientDTO> patientDTOs = new ArrayList<>();
        for (Patient patient : patientList) {
            if(patient.getMostRecentVisit() != null)
            {
                PatientDTO patientDTO = new PatientDTO();
                patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId());
                patientDTO.setAabhaId(patient.getAabhaId());
                patientDTO.setFirstName(patient.getUser().getFirstName());
                patientDTO.setLastName(patient.getUser().getLastName());
                patientDTO.setStatus(patient.getHealthStatus());
                patientDTOs.add(patientDTO);
            }
        }

        return patientDTOs;
    }
}
