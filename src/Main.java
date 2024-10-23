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

        String crearUsuario = "CREATE USER IF NOT EXISTS '" + usuario + "'@'localhost' IDENTIFIED BY '" + contraUsuario + "'";
        String activarPrivilegios = "GRANT ALL PRIVILEGES ON " + baseDeDatos + ".* TO '" + usuario + "'@'localhost'";
        String flushPrivilegios = "FLUSH PRIVILEGES";
        String crearBaseDeDatos = "CREATE DATABASE IF NOT EXISTS " + baseDeDatos;

        try (Connection conexionRoot = DriverManager.getConnection(localhost, admin, contraAdmin);
             Statement statement = conexionRoot.createStatement()) {

            statement.executeUpdate(crearBaseDeDatos);
            statement.executeUpdate(crearUsuario);
            statement.executeUpdate(activarPrivilegios);
            statement.executeUpdate(flushPrivilegios);

            System.out.println("Base de datos, usuario DAM2 y privilegios creados.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error durante la creación de la base de datos.");
            return;
        }

        String rutaBaseDeDatos = "jdbc:mysql://localhost:3306/" + baseDeDatos;
        try (Connection conexionUsuario = DriverManager.getConnection(rutaBaseDeDatos, usuario, contraUsuario);
             Statement statement = conexionUsuario.createStatement()) {

            String crearTabla = "CREATE TABLE IF NOT EXISTS CONCIERTOS (" +
                                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                            "grupo VARCHAR(100), " +
                                            "lugar VARCHAR(100), " +
                                            "fecha VARCHAR(50), " +
                                            "hora VARCHAR(50))";
            statement.executeUpdate(crearTabla);
            System.out.println("TABLA CREADA.");


            File archivoXML = new File("src/CONCIERTOS.XML");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(archivoXML);
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

                    String insertarDatos = "INSERT INTO CONCIERTOS (grupo, lugar, fecha, hora) VALUES ('" +
                            grupo + "', '" + lugar + "', '" + fecha + "', '" + hora + "')";
                    statement.executeUpdate(insertarDatos);
                }
            }

            ResultSet consulta = statement.executeQuery("SELECT * FROM CONCIERTOS");
            System.out.println("====================================================");
            System.out.println("================ TABLA CONCIERTO ===================");
            System.out.println("====================================================");
            while (consulta.next()) {
                System.out.printf("ID: %d, Grupo: %s, Lugar: %s, Fecha: %s, Hora: %s%n",
                        consulta.getInt("id"), consulta.getString("grupo"),
                        consulta.getString("lugar"), consulta.getString("fecha"),
                        consulta.getString("hora"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al conectarse a la base de datos " + baseDeDatos);
        }

        try (Connection ConexionAdmin2 = DriverManager.getConnection(localhost, admin, contraAdmin);
             Statement statement = ConexionAdmin2.createStatement()) {

            statement.executeUpdate("DROP DATABASE IF EXISTS " + baseDeDatos);
            statement.executeUpdate("DROP USER IF EXISTS '" + usuario + "'@'localhost'");
            System.out.println("Usuario DAM2 y base de datos " + baseDeDatos + " eliminados.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al eliminar el usuario o la base de datos.");
        }
    }
}

