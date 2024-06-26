package com.example.demo.services;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.demo.dto.*;
import com.example.demo.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.util.Pair;
import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FwService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleService roleService;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    GoogleDriveService googleDriveService;

    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    HospitalService hospitalService;
    @Autowired
    DoctorService doctorService;
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    AdminService adminService;
    @Autowired
    GeneralService generalService;
    @Autowired
    IdMappingRepository idMappingRepository;
    @Autowired
    FieldWorkerRepository fieldWorkerRepository;
    @Autowired
    MedicalRecordRepository medicalRecordRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    SupervisorRepository supervisorRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UpdateForPatientRepository updateForPatientRepository;
    @Autowired
    SocketIOServer server;

    public PatientDTO createPatient(PatientCreationRequest request) {
        try {
            String roleName = request.getRole().getName();
            Role role = roleService.getOrCreateRole(roleName);
            User user = new User();
            String firstName = request.getFirstName();
            String lastName = request.getLastName();
            user.setDob(request.getDob());
            user.setLastName(lastName);
            user.setFirstName(firstName);
            user.setAddress(request.getAddress());
            user.setGender(request.getGender());
            user.setRole(role);
            if (!request.getPhone().isEmpty()) {
                user.setPhone(request.getPhone());
            }
            if (request.isAssist()) {
                user.setEmail(request.getAabha());
                user.setPassword(passwordEncoder.encode("1234"));
            } else {
                String email = request.getEmail();
                String password = userService.generatePassword();
                user.setEmail(email);
                String subject = "Added as a member of Medimate India";

                String body = "Dear " + firstName + " " + lastName + ",\n\n" +
                        "You have been officially registered in the medimate India.\n\n" +
                        "Your credentials:\n" +
                        "Email: " + email + "\n" +
                        "Password: " + password + "\n\n" +
                        "Best regards,\n" +
                        "Medimate India";
                emailSenderService.sendEmail(email, subject, body);
                user.setPassword(passwordEncoder.encode(password));
            }
            User createdUser = userRepository.save(user);
            Patient patient = new Patient();

            // Create a new IdMapping object and set its privateId to the Patient's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(patient.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            patient.setUser(user);
            patient.setAabhaId(request.getAabha());
            patient.setFwAssistance(request.isAssist());
            patient.setDistrict(request.getDistrict());
            patient.setSubDivision(request.getSubDivision());
            patientRepository.save(patient);
            PatientDTO patientDTO = new PatientDTO();
            patientDTO.setPublicId(idMapping.getPublicId());
            patientDTO.setAabhaId(patient.getAabhaId());
            patientDTO.setFirstName(user.getFirstName());
            patientDTO.setLastName(user.getLastName());
            patientDTO.setStatus(patient.getHealthStatus());
            return patientDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getCategorizedClass(QuestionnaireResponseRequest request, String email) throws IOException, GeneralSecurityException {
        int n1 = 0;
        int n2 = 0;
        int score = 0;
        int id = request.getPid();
        String pid = idMappingRepository.findById(id).get().getPrivateId().toString();
        Optional<Patient> patient = patientRepository.findById(pid);
        List<AnswerResponse> answers = request.getAnswers();
        System.out.println("ANSWERS");

        System.out.println(answers);
        for(AnswerResponse answer : answers)
        {
            Optional<Question> question =  questionRepository.findById(idMappingRepository.findById(answer.getQid()).get().getPrivateId().toString());
            Answer ans = new Answer();

            // Create a new IdMapping object and set its privateId to the Answer's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(ans.getAnsId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            if(patient.isPresent())
            {
                ans.setPatient(patient.get());
            }
            if(question.isPresent())
            {
                ans.setQuestion(question.get());
                if(question.get().getType().equals("mcq"))
                {
                    ans.setMcqAns(answer.getMcqAns());

                    System.out.println("CHALL JAAA BC");

                    if(answer.getMcqAns().equals("A"))
                        score+=0;
                    else if(answer.getMcqAns().equals("B"))
                        score+=10;
                    else if(answer.getMcqAns().equals("C"))
                        score+=50;
                    else if(answer.getMcqAns().equals("D"))
                        score+=100;
                    n1++;
                }
                else if(question.get().getType().equals("range"))
                {
                    ans.setRangeAns(answer.getRangeAns());
                    score+= (10- answer.getRangeAns())*10;
                    n2++;
                }
                else if(question.get().getType().equals("descriptive"))
                {
                    ans.setSubjAns(answer.getSubjAns());
                }
                answerRepository.save(ans);
            }
        }
        System.out.println(score);
        if(score > n1*80 || score > n2*70)
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("RED");
                Hospital hospital = hospitalService.allocateHospital(patient.get().getSubDivision(), patient.get().getDistrict());
                Doctor doctor = doctorService.allocateDoctor(hospital);
                FieldWorker fieldWorker = assignFieldWorker(email);
                patient.get().setHospital(hospital);
                patient.get().setDoctor(doctor);
                patient.get().setFieldWorker(fieldWorker);
                patientRepository.save(patient.get());
                System.out.println(submitFile(request.getQnName(), id));
                System.out.println("Hospital and doc assigned");
            }
            return "RED";
        }
        else if(score > n1*31 || score > n2*31)
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("YELLOW");
                Hospital hospital = hospitalService.allocateHospital(patient.get().getSubDivision(), patient.get().getDistrict());
                Doctor doctor = doctorService.allocateDoctor(hospital);
                FieldWorker fieldWorker = assignFieldWorker(email);
                patient.get().setHospital(hospital);
                patient.get().setDoctor(doctor);
                patient.get().setFieldWorker(fieldWorker);
                patientRepository.save(patient.get());
                System.out.println(submitFile(request.getQnName(), id));
                System.out.println("Hospital and doc assigned");
            }
            return "YELLOW";
        }
        else
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("GREEN");
                patientRepository.save(patient.get());
            }
            return "GREEN";
        }
    }

    public DriveResponse uploadDescriptiveMsg(File tempFile, String qid, String pid) throws GeneralSecurityException, IOException {
        Answer answer = new Answer();

        // Create a new IdMapping object and set its privateId to the Answer's UUID
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(answer.getAnsId()));

        // Save the IdMapping object to the database
        idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

        Optional<Patient> patient = patientRepository.findById(pid);
        Optional<Question> question = questionRepository.findById(qid);
        if(question.isPresent())
        {
            DriveResponse res = googleDriveService.uploadDescriptiveMsgToDrive(tempFile);
            answer.setQuestion(question.get());
            answer.setSubjAns(generalService.encrypt(res.getUrl()));
            if(patient.isPresent())
                answer.setPatient(patient.get());
            answerRepository.save(answer);
            return res;
        }
        return null;
    }


    public String submitFile(String questionnaireName, int id) throws IOException, GeneralSecurityException {
        String patientId = idMappingRepository.findById(id).get().getPrivateId().toString();
        List<Question> questions = adminService.getAllQuestionByQnName(questionnaireName);
        Patient patient = patientRepository.findById(patientId).orElseThrow(()->new RuntimeException("Patient not found"));
        String doctorId = patient.getDoctor().getId();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(()->new RuntimeException("Doctor not found"));

        List<Map<String, Object>> questionAnswersList = new ArrayList<>();
        System.out.println("test1");

        for (Question question : questions) {
            Answer answer = answerRepository.findByQuestionIdAndPatientId(question.getId(), patientId);
            if (answer == null) continue;
            Map<String, Object> questionAnswers = new HashMap<>();
            if(question.getType().equals("mcq"))
            {
                questionAnswers.put("type", question.getType());
                questionAnswers.put("question", question.getQuestion());
                questionAnswers.put("A", question.getOptionA());
                questionAnswers.put("B", question.getOptionB());
                questionAnswers.put("C", question.getOptionA());
                questionAnswers.put("D", question.getOptionA());
                questionAnswers.put("answer", answer.getMcqAns());
            }
            else if(question.getType().equals("range"))
            {
                questionAnswers.put("type", question.getType());
                questionAnswers.put("question", question.getQuestion());
                questionAnswers.put("answer", answer.getRangeAns());
            }
            else if(answer.getSubjAns() != null)
            {
                questionAnswers.put("type", question.getType());
                questionAnswers.put("question", question.getQuestion());
                questionAnswers.put("answer", answer.getSubjAns());
            }
            questionAnswersList.add(questionAnswers);
        }

        Map<String, Object> finalJsonMap = new HashMap<>();
        finalJsonMap.put("timestamp", LocalDateTime.now().toString());
        finalJsonMap.put("type", "Questionnaire");
        finalJsonMap.put(questionnaireName, questionAnswersList);

        List<Map<String, Object>> finalJsonList = new ArrayList<>();
        finalJsonList.add(finalJsonMap);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(finalJsonList);

        Path tempFile = Files.createTempFile(generalService.encrypt(patient.getUser().getEmail()), ".json");
        Files.write(tempFile, json.getBytes());

        DriveResponse driveResponse = googleDriveService.uploadMedicalFileToDrive(tempFile.toFile());

        if (Files.exists(tempFile)) {
//            Files.delete(tempFile);
            System.out.println("File created successfully: " + tempFile.toAbsolutePath());
        } else {
            System.out.println("File does not exist: " + tempFile.toAbsolutePath());
        }

        System.out.println(driveResponse.getUrl());

        String url = generalService.encrypt(driveResponse.getUrl());
        MedicalRecord mr = new MedicalRecord();
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(mr.getUniqueId()));
        idMappingRepository.save(idMapping);
        mr.setPatient(patient);
        mr.setDoctor(doctor);
        mr.setRecord(url);
        medicalRecordRepository.save(mr);

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setPublicId(id);
        patientDTO.setAabhaId(patient.getAabhaId());
        patientDTO.setFirstName(patient.getUser().getFirstName());
        patientDTO.setLastName(patient.getUser().getLastName());
        patientDTO.setStatus(patient.getHealthStatus());
        patientDTO.setDistrict(patient.getDistrict());
        sendUpdate(patientDTO, patient.getFieldWorker().getUser().getEmail(), patient.getDoctor().getUser().getEmail(), patient.getSubDivision(), "New Patient");
        return "File uploaded successfully";
    }

    public String getFwState(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        return fw.getState();

    }

    public String getFwDistrict(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        return fw.getDistrict().getName();
    }


    public String getSubDistrict(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        return fw.getArea();
    }

    public int getPatientPublicKey(String aabha) {
        Patient patient = patientRepository.findByAabhaId(aabha);
        return idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId();
    }

    public FieldWorker assignFieldWorker(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        FwTeam team = fw.getTeam();
        if(team != null)
        {
            List<FieldWorker> fieldWorkers = fieldWorkerRepository.findByTeam_Id(team.getId());
            int mini = Integer.MAX_VALUE;
            FieldWorker assignedfFieldWorker = fw;
            for(FieldWorker worker : fieldWorkers)
            {
                int n = getNumberOfPatientsAssignedToFieldWorker(worker.getUser().getEmail());
                if(n < mini)
                {
                    mini = n;
                    assignedfFieldWorker = worker;
                }
            }
            return assignedfFieldWorker;
        }
        else
        {
            return fw;
        }
    }
    public int getNumberOfPatientsAssignedToFieldWorker(String email) {
        FieldWorker fieldWorker = fieldWorkerRepository.findByUser_Email(email);
        List<Patient> patients = patientRepository.findByFieldWorker(fieldWorker);
        return patients.size();
    }

    public PatientDTO patientLogIn(String aabhaId, String name) {
        Patient patient = patientRepository.findByAabhaId(aabhaId);
//        if(patient.getFieldWorker().getUser().getEmail().equals(name))
//        {
            PatientDTO patientDTO = new PatientDTO();
            int publicId = idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId();
            patientDTO.setPublicId(publicId);
            patientDTO.setAabhaId(patient.getAabhaId());
            patientDTO.setFirstName(patient.getUser().getFirstName());
            patientDTO.setLastName(patient.getUser().getLastName());
            patientDTO.setStatus(patient.getHealthStatus());
            return patientDTO;
//        }
//        return null;
    }

    public List<ProfileDTO> getProfiles(String email) {
        List<Notification> chats = notificationRepository.findAll();
        // Filter notifications where either sender or receiver is the provided email
        chats = chats.stream()
                .filter(chat -> chat.getSender().equals(email) || chat.getReceiver().equals(email))
                .collect(Collectors.toList());

        // Group by sender and receiver pairs and select the latest notification in each group
        Map<Pair<String, String>, Notification> latestNotifications = new HashMap<>();
        for (Notification chat : chats) {
            String e2 = chat.getReceiver();
            if(chat.getSender().equals(email))
                e2 = chat.getReceiver();
            else if(chat.getReceiver().equals(email))
                e2 = chat.getSender();
            Pair<String, String> pair = new Pair<>(email, e2);
            if (!latestNotifications.containsKey(pair) ||
                    latestNotifications.get(pair).getTimestamp().isBefore(chat.getTimestamp())) {
                latestNotifications.put(pair, chat);
            }
        }

        // Convert the selected notifications into ProfileDTO objects and return
        List<ProfileDTO> profiles = new ArrayList<>();
        for (Notification notification : latestNotifications.values()) {
            ProfileDTO profile = new ProfileDTO();
            int id = idMappingRepository.findByPrivateId(UUID.fromString(userRepository.getUserByUsername(email).getUniqueId())).getPublicId();
            int id1 = idMappingRepository.findByPrivateId(UUID.fromString(userRepository.getUserByUsername(notification.getSender()).getUniqueId())).getPublicId();
            int id2 = idMappingRepository.findByPrivateId(UUID.fromString(userRepository.getUserByUsername(notification.getReceiver()).getUniqueId())).getPublicId();
            profile.setId(id1 == id ? notification.getReceiver() : notification.getSender());
            String fullName = userRepository.getUserByUsername(id1 == id ? notification.getReceiver() : notification.getSender()).getFirstName() + " " + userRepository.getUserByUsername(id1 == id ? notification.getReceiver() : notification.getSender()).getLastName();
            profile.setName(fullName);
            LastMsgDTO lastMsgDTO = new LastMsgDTO();
            lastMsgDTO.setMsg(notification.getMessage());
            lastMsgDTO.setTime(notification.getTime());
            lastMsgDTO.setDate(notification.getDate());
            lastMsgDTO.setId(notification.getSender());
            profile.setData(lastMsgDTO);
            profiles.add(profile);
        }

        return profiles;
    }

    public Map<String, List<ChatDTO>> getAllChats(String email1, String email2){
        List<Notification> chats = notificationRepository.findAll();

        // Filter notifications based on sender and receiver
        chats = chats.stream()
                .filter(chat -> (chat.getSender().equals(email1) && chat.getReceiver().equals(email2)) ||
                        (chat.getSender().equals(email2) && chat.getReceiver().equals(email1)))
                .collect(Collectors.toList());

        // Sort the list by timestamp in ascending order
        chats.sort(Comparator.comparing(Notification::getTimestamp));

        Map<String, List<ChatDTO>> groupedChats = chats.stream()
                .collect(Collectors.groupingBy(Notification::getDate,
                        Collectors.mapping(this::convertToChatDTO, Collectors.toList())));
        return groupedChats;
    }

    private ChatDTO convertToChatDTO(Notification notification) {
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(notification.getSender());
        chatDTO.setKey(notification.getId().intValue());
        chatDTO.setData(notification.getMessage());
        chatDTO.setTime(notification.getTime());
        return chatDTO;
    }

    public SupervisorDTO getSupervisor(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        District district = fw.getDistrict();
        Supervisor supervisor = supervisorRepository.findByDistrict(district);
        SupervisorDTO supervisorDTO = new SupervisorDTO();
        supervisorDTO.setFirstName(supervisor.getUser().getFirstName());
        supervisorDTO.setLastName(supervisor.getUser().getLastName());
        supervisorDTO.setUser_id(idMappingRepository.findByPrivateId(UUID.fromString(supervisor.getUser().getUniqueId())).getPublicId());
        return supervisorDTO;
    }

    public Map<String, Object> checkFollowUp(int id, String fw_email) throws IOException {
        Patient patient = patientRepository.findById(idMappingRepository.findById(id).get().getPrivateId().toString()).orElseThrow(()->new RuntimeException("Patient not found"));
        FieldWorker fieldWorker = fieldWorkerRepository.findByUser_Email(fw_email);
        MedicalRecord mr = medicalRecordRepository.findByPatient_Id(patient.getId());
        if(patient.getFieldWorker().getUser().getEmail().equals(fw_email))
        {
            String url = generalService.decrypt(mr.getRecord());
            System.out.println(url);
            String jsonContent = googleDriveService.readJsonFromUrl(url);
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> jsonList = objectMapper.readValue(jsonContent, List.class);
            if (!jsonList.isEmpty()) {
                Map<String, Object> lastElement = jsonList.get(jsonList.size() - 1);
                return lastElement;
            }
        }
        return null;
    }

    public List<TaskDTO> viewAllTasks(String email) {
        FieldWorker fieldWorker = fieldWorkerRepository.findByUser_Email(email);
        List<Task> tasks = taskRepository.findByFieldWorkerAndStatus(fieldWorker, false);
        List<TaskDTO> taskDTOS = new ArrayList<>();
        for(Task task : tasks)
        {
            Patient patient = task.getPatient();
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setId(idMappingRepository.findByPrivateId(UUID.fromString(task.getId())).getPublicId());
            taskDTO.setPid(idMappingRepository.findByPrivateId(UUID.fromString(task.getPatient().getId())).getPublicId());
            taskDTO.setType(task.getTask_type());
            taskDTO.setDate(task.getDate());
            taskDTO.setTime(task.getTime());
            taskDTO.setAssignedTime(task.getAssignedTime());
            taskDTO.setDeadline(task.getTimestamp());
            taskDTO.setDescription(task.getDescription());
            taskDTO.setStatus(task.getStatus());
            taskDTO.setName(patient.getUser().getFirstName() + " " + patient.getUser().getLastName());
            taskDTO.setAddress(patient.getUser().getAddress());
            taskDTO.setMedicine(task.getMedicine());
            taskDTO.setTest(task.getTest());
            taskDTO.setPrecaution(task.getPrecaution());
            taskDTO.setDays(task.getDays());
            taskDTO.setAppointment(task.getAppointment());
            taskDTO.setQuestion(task.getQuestion());
            taskDTOS.add(taskDTO);
        }
        taskDTOS.sort((TaskDTO t1, TaskDTO t2) -> t2.getDeadline().compareTo(t1.getDeadline()));
        return taskDTOS;
    }

    public List<TaskDTO> viewAllTasksByDate(String email, String date) {
        FieldWorker fieldWorker = fieldWorkerRepository.findByUser_Email(email);
        List<Task> tasks = taskRepository.findByFieldWorkerAndDateAndStatus(fieldWorker, date, false);
        List<TaskDTO> taskDTOS = new ArrayList<>();
        for(Task task : tasks)
        {
            Patient patient = task.getPatient();
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setId(idMappingRepository.findByPrivateId(UUID.fromString(task.getId())).getPublicId());
            taskDTO.setPid(idMappingRepository.findByPrivateId(UUID.fromString(task.getPatient().getId())).getPublicId());
            taskDTO.setType(task.getTask_type());
            taskDTO.setDate(task.getDate());
            taskDTO.setTime(task.getTime());
            taskDTO.setAssignedTime(task.getAssignedTime());
            taskDTO.setDeadline(task.getTimestamp());
            taskDTO.setDescription(task.getDescription());
            taskDTO.setStatus(task.getStatus());
            taskDTO.setName(patient.getUser().getFirstName() + " " + patient.getUser().getLastName());
            taskDTO.setAddress(patient.getUser().getAddress());
            taskDTO.setMedicine(task.getMedicine());
            taskDTO.setTest(task.getTest());
            taskDTO.setPrecaution(task.getPrecaution());
            taskDTO.setDays(task.getDays());
            taskDTO.setAppointment(task.getAppointment());
            taskDTO.setQuestion(task.getQuestion());
            taskDTOS.add(taskDTO);
        }
        taskDTOS.sort((TaskDTO t1, TaskDTO t2) -> t2.getDeadline().compareTo(t1.getDeadline()));
        return taskDTOS;
    }

    public boolean doTask(PatientFollowUpResponse request, String email) throws IOException, GeneralSecurityException {
        int id = request.getId();
        Task tsk = taskRepository.findById(idMappingRepository.findById(id).get().getPrivateId().toString()).orElseThrow(()->new RuntimeException("Task not found"));
        FieldWorker fieldWorker = fieldWorkerRepository.findByUser_Email(email);
        if(tsk.getFieldWorker().getUser().getEmail().equals(email))
        {
            Optional<Patient> patient = patientRepository.findById(tsk.getPatient().getId());
            if(patient.isPresent())
            {
                if(!(patient.get().getAabhaId().equals(request.getAabha())))
                {
                    System.out.println("aabha id does not match");
                    return false;
                }
                if(tsk.getTask_type().equals("questionnaire"))
                {
                    Map<String, Object> newJson = new HashMap<>();
                    MedicalRecord mr = medicalRecordRepository.findByPatient_Id(patient.get().getId());
                    String url = generalService.decrypt(mr.getRecord());
                    String existingJsonContent = googleDriveService.readJsonFromUrl(url);
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> jsonList = mapper.readValue(existingJsonContent, new TypeReference<List<Map<String, Object>>>(){});
                    if (!jsonList.isEmpty()) {
                        Map<String, Object> lastElement = jsonList.get(jsonList.size() - 1);
                        Map<String, Object> doctorQuestionnaire = (Map<String, Object>) lastElement.get("doctorQuestionnaire");
                        String question = (String) doctorQuestionnaire.get("question");
                        String answer = request.getAnswer();
                        newJson.put("question", question);
                        newJson.put("answer", answer);
                        newJson.put("fieldWorker", fieldWorker.getUser().getFirstName() + " " + fieldWorker.getUser().getLastName());
                        Map<String, Object> newOuterJson = new HashMap<>();
                        newOuterJson.put("timestamp", request.getTimestamp());
                        newOuterJson.put("type", "doctorQuestionnaire");
                        newOuterJson.put("doctor", patient.get().getDoctor().getUser().getFirstName() + " " + patient.get().getDoctor().getUser().getLastName());
                        newOuterJson.put("doctorQuestionnaire", newJson);
                        jsonList.remove(jsonList.size() - 1);
                        jsonList.add(newOuterJson);

                        String updatedJsonContent = mapper.writeValueAsString(jsonList);

                        // Write the updated JSON content to a temporary file
                        Path tempFilePath = Files.createTempFile(generalService.encrypt(patient.get().getUser().getEmail()), ".json");
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
//                        sendNotification(email, patient.get().getDoctor().getUser().getEmail(), "Questionnaire is answered successfully by the patient " + patient.get().getUser().getFirstName() + " " + patient.get().getUser().getLastName());
                        PatientDTO patientDTO = new PatientDTO();
                        patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.get().getId())).getPublicId());
                        patientDTO.setAabhaId(patient.get().getAabhaId());
                        patientDTO.setFirstName(patient.get().getUser().getFirstName());
                        patientDTO.setLastName(patient.get().getUser().getLastName());
                        patientDTO.setStatus(patient.get().getHealthStatus());
                        patientDTO.setDistrict(patient.get().getDistrict());
                        sendUpdate(patientDTO, patient.get().getFieldWorker().getUser().getEmail(), patient.get().getDoctor().getUser().getEmail(), patient.get().getSubDivision(), "Questionnaire Answered");
                        tsk.setStatus(true);
                        taskRepository.save(tsk);
                        return true;
                    }
                    else {
                        return false;
                    }

                }
                else if(tsk.getTask_type().equals("prescription"))
                {
                    MedicalRecord mr = medicalRecordRepository.findByPatient_Id(patient.get().getId());
                    String url = generalService.decrypt(mr.getRecord());
                    String existingJsonContent = googleDriveService.readJsonFromUrl(url);
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> jsonList = mapper.readValue(existingJsonContent, new TypeReference<List<Map<String, Object>>>(){});
                    if (!jsonList.isEmpty()) {
                        Map<String, Object> lastElement = jsonList.get(jsonList.size() - 1);
                        Map<String, Object> preescriptionMap = (Map<String, Object>) lastElement.get("prescription");
                        int days = (int) preescriptionMap.get("days");
                        Map<String, Object> newJson = new HashMap<>();
                        newJson.put("timestamp", request.getTimestamp());
                        newJson.put("type", "prescriptionUpdate");
                        newJson.put("fieldWorker", fieldWorker.getUser().getFirstName() + " " + fieldWorker.getUser().getLastName());
                        newJson.put("status", "completed");
                        jsonList.add(newJson);

                        String updatedJsonContent = mapper.writeValueAsString(jsonList);

                        // Write the updated JSON content to a temporary file
                        Path tempFilePath = Files.createTempFile(generalService.encrypt(patient.get().getUser().getEmail()), ".json");
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
//                        sendNotification(email, patient.get().getDoctor().getUser().getEmail(), "Prescription is given successfully to the patient " + patient.get().getUser().getFirstName() + " " + patient.get().getUser().getLastName());
                        PatientDTO patientDTO = new PatientDTO();
                        patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.get().getId())).getPublicId());
                        patientDTO.setAabhaId(patient.get().getAabhaId());
                        patientDTO.setFirstName(patient.get().getUser().getFirstName());
                        patientDTO.setLastName(patient.get().getUser().getLastName());
                        patientDTO.setStatus(patient.get().getHealthStatus());
                        patientDTO.setDistrict(patient.get().getDistrict());
                        sendUpdate(patientDTO, patient.get().getFieldWorker().getUser().getEmail(), patient.get().getDoctor().getUser().getEmail(), patient.get().getSubDivision(), "Prescription Given");
                        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                        System.out.println("scheduling delayed task");
                        Runnable task = () -> {
//                            sendNotification(email, patient.get().getDoctor().getUser().getEmail(), "Prescription Update, Please take status for the prescription of the patient " + patient.get().getUser().getFirstName() + " " + patient.get().getUser().getLastName());
                            sendUpdate(patientDTO, patient.get().getFieldWorker().getUser().getEmail(), patient.get().getDoctor().getUser().getEmail(), patient.get().getSubDivision(), "Prescription Update");
                            System.out.println("executed delayed task");
                        };
                        executor.schedule(task, 1, TimeUnit.MINUTES);
                        tsk.setStatus(true);
                        taskRepository.save(tsk);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else if(tsk.getTask_type().equals("appointment_for_field_worker"))
                {
                    MedicalRecord mr = medicalRecordRepository.findByPatient_Id(patient.get().getId());
                    String url = generalService.decrypt(mr.getRecord());
                    String existingJsonContent = googleDriveService.readJsonFromUrl(url);
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> jsonList = mapper.readValue(existingJsonContent, new TypeReference<List<Map<String, Object>>>(){});
                    if (!jsonList.isEmpty()) {
                        Map<String, Object> newJson = new HashMap<>();
                        newJson.put("timestamp", request.getTimestamp());
                        newJson.put("type", "appointmentUpdate");
                        newJson.put("fieldWorker", fieldWorker.getUser().getFirstName() + " " + fieldWorker.getUser().getLastName());
                        newJson.put("status", "completed");
                        jsonList.add(newJson);

                        String updatedJsonContent = mapper.writeValueAsString(jsonList);

                        // Write the updated JSON content to a temporary file
                        Path tempFilePath = Files.createTempFile(generalService.encrypt(patient.get().getUser().getEmail()), ".json");
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

                        PatientDTO patientDTO = new PatientDTO();
                        patientDTO.setPublicId(idMappingRepository.findByPrivateId(UUID.fromString(patient.get().getId())).getPublicId());
                        patientDTO.setAabhaId(patient.get().getAabhaId());
                        patientDTO.setFirstName(patient.get().getUser().getFirstName());
                        patientDTO.setLastName(patient.get().getUser().getLastName());
                        patientDTO.setStatus(patient.get().getHealthStatus());
                        patientDTO.setDistrict(patient.get().getDistrict());
                        sendUpdate(patientDTO, patient.get().getFieldWorker().getUser().getEmail(), patient.get().getDoctor().getUser().getEmail(), patient.get().getSubDivision(), "Appointment Given");
//                        sendNotification(email, patient.get().getDoctor().getUser().getEmail(), "Appointment is given successfully to the patient " + patient.get().getUser().getFirstName() + " " + patient.get().getUser().getLastName());
                        tsk.setStatus(true);
                        taskRepository.save(tsk);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private void sendUpdate(PatientDTO dto, String sender, String receiver, String area, String msg)
    {
        UpdateForPatient updateForPatient = getOrCreateUpdateForPatient(dto.getPublicId());
        updateForPatient.setAabhaId(dto.getAabhaId());
        updateForPatient.setFirstName(dto.getFirstName());
        updateForPatient.setLastName(dto.getLastName());
        updateForPatient.setDistrict(dto.getDistrict());
        updateForPatient.setStatus(dto.getStatus());
        updateForPatient.setPid(dto.getPublicId());
        updateForPatient.setSender(sender);
        updateForPatient.setReceiver(receiver);
        updateForPatient.setMessage(msg);
        updateForPatient.setArea(area);
        LocalDateTime timestamp = LocalDateTime.now().plusDays(2);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = timestamp.format(dateFormatter);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = timestamp.format(timeFormatter);
        updateForPatient.setDate(date);
        updateForPatient.setTime(time);
        updateForPatient.setIsRead(false);

        Collection<SocketIOClient> allClients = server.getAllClients();
        for (SocketIOClient client : allClients) {
            String email = client.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
            if (email.equals(receiver)){
                Collection<String>allRooms = client.getAllRooms();
                for(String room : allRooms) {
                    if(room.equals("NotificationRoom"))
                        client.sendEvent("receive_notification", updateForPatient);
                }
            }
        }

        updateForPatientRepository.save(updateForPatient);
    }

    UpdateForPatient getOrCreateUpdateForPatient(int pid) {
        UpdateForPatient updateForPatient = updateForPatientRepository.findByPid(pid);
        if (updateForPatient == null) {
            updateForPatient = new UpdateForPatient();
            return updateForPatient;
        }
        return updateForPatient;
    }

    @Scheduled(fixedRate = 60*60*1000)
    public void reAssignTask()
    {
        List<Task> tasks = taskRepository.findAll();
        for(Task task : tasks)
        {
            Patient patient = task.getPatient();
            if(task.getTimestamp().isBefore(LocalDateTime.now()) && !task.getStatus())
            {
                patient.getFieldWorker().setTaskMissed(patient.getFieldWorker().getTaskMissed()+1);
                task.setTimestamp(LocalDateTime.now().plusDays(2));
                Collection<SocketIOClient> allClients = server.getAllClients();
                for (SocketIOClient client : allClients) {
                    String email = client.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
                    Supervisor supervisor = supervisorRepository.findByDistrict(patient.getFieldWorker().getDistrict());
                    Notification notification = new Notification();
                    notification.setSender(patient.getUser().getEmail());
                    notification.setReceiver(supervisor.getUser().getEmail());
                    notification.setMessage("I have not completed my task on time with task id "+task.getId());
                    notification.setTimestamp(LocalDateTime.now());

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String date = notification.getTimestamp().format(dateFormatter);

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    String time = notification.getTimestamp().format(timeFormatter);
                    notification.setDate(date);
                    notification.setTime(time);
                    notification.setIsRead(false);
                    if (email.equals(supervisor.getUser().getEmail())){
                        Collection<String>allRooms = client.getAllRooms();
                        for(String room : allRooms) {
                            if(room.equals("NotificationRoom"))
                                client.sendEvent("read_message", notification);
                        }
                    }
                }
            }
        }
    }
}
