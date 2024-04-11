package com.example.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
}
