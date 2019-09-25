/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
*/
package com.boomi.util;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class DataGenerator {
    // Logger initialization
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    public static void main(String[] args) throws Exception {
        // Get the Program parameters
        CommonParams.init(args);
        final String dataFile = CommonParams.getParam(Constants.DATA_FILE);
       convertCsvToJson(dataFile);
    }

  /**
   * 
   * @param fileName
   * @return
   * @throws Exception
   */
    public static String convertCsvToJson(String fileName) throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(fileName);

        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        CsvMapper csvMapper = new CsvMapper();

        // Read data from CSV file
        List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(inputStream).readAll();

        ObjectMapper mapper = new ObjectMapper();

        // Write JSON formated data to stdout
        String result = mapper.writeValueAsString(readAll);

        LOG.debug("@@@@@@@@@@@@@ DATA  @@@@@@@@@@@@@  "+result);

        return result;
    }

  /**
   * 
   * @param fileName
   * @return
   */
    private File getFileFromResources(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }

    }
}