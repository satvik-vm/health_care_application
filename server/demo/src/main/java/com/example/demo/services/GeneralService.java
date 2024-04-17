package com.example.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GeneralService {

    public List<String> getStates() throws IOException {
        List<String> states = new ArrayList<>();

        // Path to your JSON file
        String currentDirectory = System.getProperty("user.dir");
        System.setProperty("user.dir", currentDirectory + "/../Json");
        String relativePath = "demo/src/main/java/com/example/demo/Json/5States_Combined.json";
        String jsonFilePath = new File(relativePath).getAbsolutePath();
        // Create ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Read JSON file and map to a Map<String, Map<String, Map<String, Map<String, Object>>>> structure
        Map<String, Map<String, Map<String, Map<String, Object>>>> data = mapper.readValue(new File(jsonFilePath), Map.class);

        // Extract state names and add to the list
        for (String state : data.keySet()) {
            states.add(state);
        }

        return states;
    }

    public List<String> getDistrictsByState(String stateName) {
        List<String> districts = new ArrayList<>();

        try {
            // Path to your JSON file
            String currentDirectory = System.getProperty("user.dir");
            System.setProperty("user.dir", currentDirectory + "/../Json");
            String relativePath = "demo/src/main/java/com/example/demo/Json/5States_Combined.json";
            String jsonFilePath = new File(relativePath).getAbsolutePath();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            JsonNode stateNode = rootNode.get(stateName);
            if (stateNode != null) {
                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = stateNode.fields();
                while (fieldsIterator.hasNext()) {
                    Map.Entry<String, JsonNode> districtEntry = fieldsIterator.next();
                    districts.add(districtEntry.getKey());
                }
            } else {
                System.out.println("State not found in the JSON data.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return districts;
    }

    public List<String> getSubdistrictsByStateAndDistrict(String stateName, String districtName) {
        List<String> subdistricts = new ArrayList<>();

        try {
            String currentDirectory = System.getProperty("user.dir");
            System.setProperty("user.dir", currentDirectory + "/../Json");
            String relativePath = "demo/src/main/java/com/example/demo/Json/5States_Combined.json";
            String jsonFilePath = new File(relativePath).getAbsolutePath();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            JsonNode stateNode = rootNode.get(stateName);
            if (stateNode != null) {
                JsonNode districtNode = stateNode.get(districtName);
                if (districtNode != null) {
                    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = districtNode.fields();
                    while (fieldsIterator.hasNext()) {
                        Map.Entry<String, JsonNode> subdistrictEntry = fieldsIterator.next();
                        subdistricts.add(subdistrictEntry.getKey());
                    }
                } else {
                    System.out.println("District not found in the JSON data.");
                }
            } else {
                System.out.println("State not found in the JSON data.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subdistricts;
    }

    public JsonNode getLocation() {
        try {
            String currentDirectory = System.getProperty("user.dir");
            System.setProperty("user.dir", currentDirectory + "/../Json");
            String relativePath = "demo/src/main/java/com/example/demo/Json/5States_Combined.json";
            String jsonFilePath = new File(relativePath).getAbsolutePath();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(new File(jsonFilePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<JsonNode> getHospitals(String state, String district, String subdistrict) {
        List<JsonNode> hospitals = new ArrayList<>();

        // Load the JSON file
        String currentDirectory = System.getProperty("user.dir");
        System.setProperty("user.dir", currentDirectory + "/../Json");
        String relativePath = "demo/src/main/java/com/example/demo/Json/Updated_hospital_data_doctors_5States.json";
        String jsonFilePath = new File(relativePath).getAbsolutePath();
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            JsonNode data = mapper.readTree(file);

            // Get the hospitals for the specified location
            JsonNode stateData = data.get(state);
            if (stateData != null) {
                JsonNode districtData = stateData.get(district);
                if (districtData != null) {
                    JsonNode subdistrictData = districtData.get(subdistrict);
                    if (subdistrictData != null) {
                        Iterator<String> hospitalNames = subdistrictData.fieldNames();
                        while (hospitalNames.hasNext()) {
                            String hospitalName = hospitalNames.next();
                            JsonNode hospitalDetails = subdistrictData.get(hospitalName);
                            hospitals.add(createHospitalObject(hospitalName, hospitalDetails));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hospitals;
    }

    private JsonNode createHospitalObject(String hospitalName, JsonNode hospitalDetails) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode().set(hospitalName, hospitalDetails);
    }

    public String encrypt(String url) {
        try {
            String key = "1MvOeTDF4wsWFRwbL8EKxw=="; // Replace this with your actual key
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(url.getBytes(StandardCharsets.UTF_8));
            String encryptedUrl = Base64.getEncoder().encodeToString(encryptedBytes);

            return encryptedUrl;
        } catch (Exception e) {
            // Handle exceptions properly in your application
            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(String encryptedUrl) {
        try {
            String key = "1MvOeTDF4wsWFRwbL8EKxw=="; // Replace this with your actual key
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBase64 = Base64.getDecoder().decode(encryptedUrl);
            byte[] decryptedBytes = cipher.doFinal(decodedBase64);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Handle exceptions properly in your application
            e.printStackTrace();
            return null;
        }
    }


}
