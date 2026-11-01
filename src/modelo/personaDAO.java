package modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class personaDAO {

    private static final String RUTA_DIRECTORIO = "src/modelo/gestionContactos";
    private static final String NOMBRE_ARCHIVO = "datosContactos.csv";

    private final File archivo;
    private persona persona;

    public personaDAO() {
        this(null);
    }

    public personaDAO(persona persona) {
        this.persona = persona;
        File directorio = new File(RUTA_DIRECTORIO);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        this.archivo = new File(directorio, NOMBRE_ARCHIVO);
        prepararArchivo();
    }

    private void prepararArchivo() {
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean escribirArchivo() {
        return persona != null && escribirArchivo(this.persona);
    }

    public boolean escribirArchivo(persona nuevaPersona) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true))) {
            writer.write(nuevaPersona.datosContacto());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<persona> leerArchivo() throws IOException {
        List<persona> personas = new ArrayList<>();
        if (!archivo.exists() || archivo.length() == 0) {
            return personas;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length == 5) {
                    personas.add(new persona(
                            datos[0],
                            datos[1],
                            datos[2],
                            datos[3],
                            Boolean.parseBoolean(datos[4])
                    ));
                }
            }
        }
        return personas;
    }

    public void actualizarContactos(List<persona> personas) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, false))) {
            for (persona p : personas) {
                writer.write(p.datosContacto());
                writer.newLine();
            }
        }
    }
}