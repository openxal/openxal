/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.emittanceanalysis.script;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import xal.extension.emscan.Scanner;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;

/**
 *
 * @author az9
 */
public class ScanMain {

    public static void main(String[] args) throws ConnectionException, MonitorException, InterruptedException, FileNotFoundException, IOException {

        Properties p = new Properties();
        p.load(new FileInputStream(args[0]));

        Map<String, String> propMap = new HashMap<>();

        for (String key : p.stringPropertyNames()) {
            propMap.put(key, p.getProperty(key));
        }
        Scanner scanner = new Scanner();
        scanner.setPaused(false);
        scanner.run(propMap);
        System.exit(0);

    }
}
