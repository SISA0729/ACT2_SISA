import java.sql.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Main {
    public static void main(String[] args) {
        String localhost = "jdbc:mysql://localhost:3306/";
        String admin = "root";
        String contraAdmin = "";

        String usuario = "DAM2";
        String contraUsuario = "DAM2";
        String baseDeDatos = "BD_SISA";

        // Queries para crear el usuario y la base de datos
        String createUserQuery = "CREATE USER IF NOT EXISTS '" + usuario + "'@'localhost' IDENTIFIED BY '" + contraUsuario + "'";
        String grantPrivilegesQuery = "GRANT ALL PRIVILEGES ON " + baseDeDatos + ".* TO '" + usuario + "'@'localhost'";
        String flushPrivilegesQuery = "FLUSH PRIVILEGES";
        String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + baseDeDatos;

        // Conectarse como administrador para crear la base de datos y el usuario
        try (Connection adminConnection = DriverManager.getConnection(localhost, admin, contraAdmin);
             Statement statement = adminConnection.createStatement()) {

            statement.executeUpdate(createDatabaseQuery);
            System.out.println("Base de datos " + baseDeDatos + " creada o ya existente.");

            statement.executeUpdate(createUserQuery);
            System.out.println("Usuario DAM2 creado o ya existente.");

            statement.executeUpdate(grantPrivilegesQuery);
            System.out.println("Permisos concedidos a DAM2 sobre la base de datos " + baseDeDatos);

            statement.executeUpdate(flushPrivilegesQuery);
            System.out.println("Privilegios aplicados.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error durante la creación de la base de datos, usuario o la concesión de permisos.");
            return;
        }

        // Conexión como el nuevo usuario DAM2
        String userUrl = "jdbc:mysql://localhost:3306/" + baseDeDatos;
        try (Connection userConnection = DriverManager.getConnection(userUrl, usuario, contraUsuario);
             Statement statement = userConnection.createStatement()) {

            System.out.println("Conexión exitosa a la base de datos " + baseDeDatos + " como usuario DAM2.");

            // Crear la tabla CONCIERTOS
            String createTableQuery = "CREATE TABLE IF NOT EXISTS CONCIERTOS (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "grupo VARCHAR(100), " +
                    "lugar VARCHAR(100), " +
                    "fecha VARCHAR(50), " +
                    "hora VARCHAR(50))";
            statement.executeUpdate(createTableQuery);
            System.out.println("Tabla CONCIERTOS creada.");

            // Leer el archivo XML y extraer los datos de conciertos
            File xmlFile = new File("src/CONCIERTOS.XML");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList conciertosList = document.getElementsByTagName("Concierto");
            for (int i = 0; i < conciertosList.getLength(); i++) {
                Node node = conciertosList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element concierto = (Element) node;
                    String grupo = concierto.getElementsByTagName("Grupo").item(0).getTextContent();
                    String lugar = concierto.getElementsByTagName("Lugar").item(0).getTextContent();
                    String fecha = concierto.getElementsByTagName("Fecha").item(0).getTextContent();
                    String hora = concierto.getElementsByTagName("Hora").item(0).getTextContent();

                    // Insertar los datos en la tabla CONCIERTOS
                    String insertQuery = "INSERT INTO CONCIERTOS (grupo, lugar, fecha, hora) VALUES ('" +
                            grupo + "', '" + lugar + "', '" + fecha + "', '" + hora + "')";
                    statement.executeUpdate(insertQuery);
                }
            }
            System.out.println("Datos insertados desde el fichero CONCIERTOS.XML.");

            // Realizar una consulta de los registros en la tabla CONCIERTOS y mostrarlos
            ResultSet resultSet = statement.executeQuery("SELECT * FROM CONCIERTOS");
            System.out.println("Registros de la tabla CONCIERTOS:");
            while (resultSet.next()) {
                System.out.printf("ID: %d, Grupo: %s, Lugar: %s, Fecha: %s, Hora: %s%n",
                        resultSet.getInt("id"), resultSet.getString("grupo"),
                        resultSet.getString("lugar"), resultSet.getString("fecha"),
                        resultSet.getString("hora"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al conectarse o al manejar la base de datos " + baseDeDatos);
        }

        // Eliminar el usuario y la base de datos
        try (Connection adminConnection = DriverManager.getConnection(localhost, admin, contraAdmin);
             Statement statement = adminConnection.createStatement()) {

            statement.executeUpdate("DROP DATABASE IF EXISTS " + baseDeDatos);
            statement.executeUpdate("DROP USER IF EXISTS '" + usuario + "'@'localhost'");
            System.out.println("Usuario DAM2 y base de datos " + baseDeDatos + " eliminados.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al eliminar el usuario o la base de datos.");
        }
    }
}

