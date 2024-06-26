/*
 * Copyright (C) 2015 The Pennsylvania State University and the University of Wisconsin
 * Systems and Internet Infrastructure Security Laboratory
 *
 * Author: Damien Octeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.psu.cse.siis.ic3.db;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public abstract class Table {
  private static final String SELECT_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";
  private static final int MYSQL_PORT = 3306;
  protected static final String ID = "id";
  protected static final String[] AUTOGENERATED_ID = new String[] { ID };

  private static String url = null;
  private static Session session = null;
  private static Connection connection = null;

  private static String sshPropertiesPath;
  private static String dbPropertiesPath;
  private static int localPort;

  protected static final int NOT_FOUND = Constants.NOT_FOUND;

  protected String insertString;
  protected String findString;
  protected String batchInsertString;
  protected String batchFindString;
  protected PreparedStatement insertStatement = null;
  protected PreparedStatement findStatement = null;
  private PreparedStatement selectLastInsertId = null;

  public static void init(String dbName, String dbPropertiesPath, String sshPropertiesPath,
      int localPort) {
    Table.sshPropertiesPath = sshPropertiesPath;
    Table.dbPropertiesPath = dbPropertiesPath;
    Table.localPort = localPort;
    url =
        sshPropertiesPath != null ? "jdbc:mysql://localhost:" + localPort + "/" + dbName
            : "jdbc:mysql://localhost/" + dbName;
  }

  public static Connection getConnection() {
    connect();
    return connection;
  }

  public static void closeConnection() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    if (session != null && session.isConnected()) {
      session.disconnect();
    }
  }

  private static void makeSshTunnel() throws IOException, NumberFormatException, JSchException {
    if (session != null && session.isConnected()) {
      return;
    }

    Properties sshProperties = new Properties();
    if (sshPropertiesPath.startsWith("/db/")) {
      sshProperties.load(Table.class.getResourceAsStream(sshPropertiesPath));
    } else {
      sshProperties.load(new FileReader(sshPropertiesPath));
    }

    JSch jSch = new JSch();
    String host = sshProperties.getProperty("host");
    session =
        jSch.getSession(sshProperties.getProperty("user"), host,
            Integer.valueOf(sshProperties.getProperty("port")));
    session.setConfig("StrictHostKeyChecking", "no");
    jSch.addIdentity(sshProperties.getProperty("identity"));
    session.connect();
    session.setPortForwardingL(localPort, host, MYSQL_PORT);
  }

  private static void connect() {
    if (url == null) {
      throw new RuntimeException(
          "Method init() should be called first to initialize database connection");
    }

    if (sshPropertiesPath != null) {
      try {
        makeSshTunnel();
      } catch (NumberFormatException | IOException | JSchException e) {
        e.printStackTrace();
        return;
      }
    }

    try {
      if (connection != null && !connection.isClosed()) {
        return;
      }
    } catch (SQLException e2) {
      e2.printStackTrace();
    }

    Properties properties = new Properties();

    try {
      if (dbPropertiesPath.startsWith("/db/")) {
        properties.load(SQLConnection.class.getResourceAsStream(dbPropertiesPath));
      } else {
        properties.load(new FileReader(dbPropertiesPath));
      }
    } catch (IOException e1) {
      e1.printStackTrace();
      return;
    }

    try {
      Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      e.printStackTrace();
      return;
    }

    try {
      connection = DriverManager.getConnection(url, properties);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }
  }

  protected int findAutoIncrement() throws SQLException {
    connect();
    if (selectLastInsertId == null || selectLastInsertId.isClosed()) {
      selectLastInsertId = connection.prepareStatement(SELECT_LAST_INSERT_ID);
    }
    ResultSet resultSet = selectLastInsertId.executeQuery();
    int autoinc;
    if (resultSet.next()) {
      autoinc = resultSet.getInt("LAST_INSERT_ID()");
    } else {
      autoinc = NOT_FOUND;
    }
    resultSet.close();
    return autoinc;
  }

  protected int processIntFindQuery(PreparedStatement statement) throws SQLException {
    return processIntFindQuery(statement, "id");
  }

  protected int processIntFindQuery(PreparedStatement statement, String column) throws SQLException {
    ResultSet resultSet = statement.executeQuery();
    int result;
    if (resultSet.next()) {
      result = resultSet.getInt(column);
    } else {
      result = NOT_FOUND;
    }
    resultSet.close();
    return result;
  }
}
