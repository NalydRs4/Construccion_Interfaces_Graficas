package controlador;

import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import modelo.persona;
import modelo.personaDAO;
import vista.ventana;

public class logica_ventana implements ActionListener {

    private final ventana delegado;
    private final personaDAO dao;
    private List<persona> contactos;
    private TableRowSorter<DefaultTableModel> sorter;

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;
        this.dao = new personaDAO();

        cargarContactos();
        configurarBotones();
        configurarCambioIdioma();
        configurarMenuContextualYTabla();
        configurarAtajosTeclado();
        configurarBuscador();
    }

    private void configurarBotones() {
        delegado.btn_add.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_limpiar.addActionListener(this);
    }

    private void configurarCambioIdioma() {
        delegado.cmb_idioma.addActionListener(e -> {
            String seleccionado = String.valueOf(delegado.cmb_idioma.getSelectedItem());
            Locale nuevoIdioma;
            switch (seleccionado) {
                case "English":
                    nuevoIdioma = new Locale("en", "US");
                    break;
                case "Kichwa":
                    nuevoIdioma = new Locale("qu", "EC");
                    break;
                default:
                    nuevoIdioma = new Locale("es", "ES");
                    break;
            }
            delegado.dispose();
            new ventana(nuevoIdioma).setVisible(true);
        });
    }

    private void configurarMenuContextualYTabla() {
        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem itemModificar = new JMenuItem("Modificar Contacto");
        JMenuItem itemEliminar = new JMenuItem("Eliminar Contacto");
        menuContextual.add(itemModificar);
        menuContextual.add(itemEliminar);

        itemModificar.addActionListener(e -> delegado.btn_modificar.doClick());
        itemEliminar.addActionListener(e -> delegado.btn_eliminar.doClick());

        delegado.tbl_contactos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = delegado.tbl_contactos.rowAtPoint(e.getPoint());
                if (row >= 0 && row < delegado.tbl_contactos.getRowCount()) {
                    delegado.tbl_contactos.setRowSelectionInterval(row, row);
                    cargarContactoEnFormulario();
                    if (SwingUtilities.isRightMouseButton(e)) {
                        menuContextual.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else {
                    limpiarCampos();
                }
            }
        });

        MouseAdapter deseleccionarListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                limpiarCampos();
            }
        };
        delegado.contentPane.addMouseListener(deseleccionarListener);
        delegado.tbl_contactos.getParent().addMouseListener(deseleccionarListener);
    }

    private void configurarAtajosTeclado() {
        asignarAtajo(delegado.contentPane, JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyEvent.VK_ENTER, "accionEnter", () -> {
                    if (delegado.txt_buscar.isFocusOwner()) {
                        buscarPorNombre(true);
                    } else {
                        delegado.btn_add.doClick();
                    }
                });

        asignarAtajo(delegado.tbl_contactos, JComponent.WHEN_FOCUSED,
                KeyEvent.VK_DELETE, "eliminar", () -> delegado.btn_eliminar.doClick());
    }

    private void asignarAtajo(JComponent componente, int condicion, int tecla, String nombreAccion, Runnable accion) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(tecla, 0);
        componente.getInputMap(condicion).put(keyStroke, nombreAccion);
        componente.getActionMap().put(nombreAccion, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.run();
            }
        });
    }

    private void configurarBuscador() {
        sorter = new TableRowSorter<>(delegado.modeloTabla);
        delegado.tbl_contactos.setRowSorter(sorter);

        delegado.txt_buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { buscarPorNombre(false); }
            @Override
            public void removeUpdate(DocumentEvent e) { buscarPorNombre(false); }
            @Override
            public void changedUpdate(DocumentEvent e) { buscarPorNombre(false); }
        });
    }

    private void buscarPorNombre(boolean mostrarAlertaNoExiste) {
        String texto = delegado.txt_buscar.getText().trim().toLowerCase();

        if (texto.isEmpty()) {
            limpiarCampos();
            return;
        }

        for (int i = 0; i < delegado.tbl_contactos.getRowCount(); i++) {
            String nombreFila = String.valueOf(delegado.tbl_contactos.getValueAt(i, 0)).toLowerCase();
            if (nombreFila.contains(texto)) {
                delegado.tbl_contactos.setRowSelectionInterval(i, i);
                delegado.tbl_contactos.scrollRectToVisible(delegado.tbl_contactos.getCellRect(i, 0, true));
                cargarContactoEnFormulario();
                return;
            }
        }

        limpiarCampos();
        if (mostrarAlertaNoExiste) {
            JOptionPane.showMessageDialog(delegado, "El contacto no existe.");
        }
    }

    private void cargarContactos() {
        try {
            contactos = dao.leerArchivo();
            delegado.modeloTabla.setRowCount(0);

            String idiomaActual = String.valueOf(delegado.cmb_idioma.getSelectedItem());
            String textoSi = "English".equals(idiomaActual) ? "Yes" : "Kichwa".equals(idiomaActual) ? "Arí" : "Sí";
            String textoNo = "Kichwa".equals(idiomaActual) ? "Mana" : "No";

            for (persona c : contactos) {
                Object[] fila = {
                    c.getNombre(),
                    c.getTelefono(),
                    c.getEmail(),
                    traducirCategoria(c.getCategoria()),
                    c.isFavorito() ? textoSi : textoNo
                };
                delegado.modeloTabla.addRow(fila);
            }
            actualizarEstadisticas();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(delegado, "Error al cargar los contactos.");
        }
    }

    private void limpiarCampos() {
        delegado.txt_nombres.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        delegado.cmb_categoria.setSelectedIndex(0);
        delegado.chb_favorito.setSelected(false);
        delegado.tbl_contactos.clearSelection();
    }

    private void cargarContactoEnFormulario() {
        int vistaFila = delegado.tbl_contactos.getSelectedRow();
        if (vistaFila >= 0) {
            int modeloFila = delegado.tbl_contactos.convertRowIndexToModel(vistaFila);
            persona p = contactos.get(modeloFila);
            delegado.txt_nombres.setText(p.getNombre());
            delegado.txt_telefono.setText(p.getTelefono());
            delegado.txt_email.setText(p.getEmail());
            delegado.cmb_categoria.setSelectedItem(traducirCategoria(p.getCategoria()));
            delegado.chb_favorito.setSelected(p.isFavorito());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object fuente = e.getSource();
        if (fuente == delegado.btn_limpiar) {
            limpiarCampos();
        } else if (fuente == delegado.btn_add) {
            agregarContacto();
        } else if (fuente == delegado.btn_modificar) {
            modificarContacto();
        } else if (fuente == delegado.btn_eliminar) {
            eliminarContacto();
        }
    }

    private boolean camposSonValidos(String nombres, String telefono, String email, String categoria) {
        String categoriaDefecto = delegado.textos.getString("cmb.categoria.defecto");
        if (nombres.isEmpty() || telefono.isEmpty() || email.isEmpty()
                || categoria.isEmpty() || categoria.equals(categoriaDefecto)) {
            JOptionPane.showMessageDialog(delegado, "Todos los campos son obligatorios.");
            return false;
        }
        return true;
    }

    private persona obtenerDatosFormulario() {
        String nombres = delegado.txt_nombres.getText().trim();
        String telefono = delegado.txt_telefono.getText().trim();
        String email = delegado.txt_email.getText().trim();
        String categoria = delegado.cmb_categoria.getSelectedItem() != null
                ? delegado.cmb_categoria.getSelectedItem().toString()
                : "";
        boolean favorito = delegado.chb_favorito.isSelected();

        if (!camposSonValidos(nombres, telefono, email, categoria)) {
            return null;
        }
        return new persona(nombres, telefono, email, categoria, favorito);
    }

    private void agregarContacto() {
        persona nuevoContacto = obtenerDatosFormulario();
        if (nuevoContacto == null) {
            return;
        }

        delegado.btn_add.setEnabled(false);
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                delegado.prg_carga.setVisible(true);
                delegado.prg_carga.setValue(0);
            });

            for (int i = 0; i <= 100; i++) {
                final int progreso = i;
                SwingUtilities.invokeLater(() -> delegado.prg_carga.setValue(progreso));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            SwingUtilities.invokeLater(() -> {
                if (dao.escribirArchivo(nuevoContacto)) {
                    JOptionPane.showMessageDialog(delegado, delegado.textos.getString("msg.exito"));
                    refrescarVista();
                } else {
                    JOptionPane.showMessageDialog(delegado, "Error al registrar el contacto.");
                }
                delegado.prg_carga.setValue(0);
                delegado.btn_add.setEnabled(true);
            });
        }).start();
    }

    private void modificarContacto() {
        int vistaFila = delegado.tbl_contactos.getSelectedRow();
        if (vistaFila < 0) {
            JOptionPane.showMessageDialog(delegado, "Seleccione un contacto de la tabla para modificar.");
            return;
        }

        persona datosActualizados = obtenerDatosFormulario();
        if (datosActualizados == null) {
            return;
        }

        int modeloFila = delegado.tbl_contactos.convertRowIndexToModel(vistaFila);
        contactos.set(modeloFila, datosActualizados);

        try {
            dao.actualizarContactos(contactos);
            JOptionPane.showMessageDialog(delegado, "Contacto actualizado correctamente.");
            refrescarVista();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(delegado, "Error al actualizar el contacto.");
        }
    }

    private void eliminarContacto() {
        int vistaFila = delegado.tbl_contactos.getSelectedRow();
        if (vistaFila < 0) {
            JOptionPane.showMessageDialog(delegado, "Seleccione un contacto de la tabla para eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                delegado,
                "¿Seguro que desea eliminar este contacto?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            int modeloFila = delegado.tbl_contactos.convertRowIndexToModel(vistaFila);
            contactos.remove(modeloFila);
            try {
                dao.actualizarContactos(contactos);
                JOptionPane.showMessageDialog(delegado, "Contacto eliminado.");
                refrescarVista();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(delegado, "Error al eliminar el contacto.");
            }
        }
    }

    private void refrescarVista() {
        cargarContactos();
        limpiarCampos();
    }

    private String traducirCategoria(String categoriaGuardada) {
        if (categoriaGuardada == null) {
            return "";
        }
        switch (categoriaGuardada.toLowerCase()) {
            case "familia":
            case "family":
            case "ayllu":
                return delegado.textos.getString("cat.familia");
            case "amigos":
            case "friends":
            case "masiykuna":
                return delegado.textos.getString("cat.amigos");
            case "trabajo":
            case "work":
            case "llamkay":
                return delegado.textos.getString("cat.trabajo");
            default:
                return categoriaGuardada;
        }
    }

    private void actualizarEstadisticas() {
        if (contactos == null) {
            return;
        }

        delegado.lbl_totalContactos.setText("Total de contactos: " + contactos.size());

        String catFamilia = delegado.textos.getString("cat.familia");
        String catAmigos = delegado.textos.getString("cat.amigos");
        String catTrabajo = delegado.textos.getString("cat.trabajo");

        int fam = 0, ami = 0, tra = 0;
        for (persona c : contactos) {
            String cat = traducirCategoria(c.getCategoria());
            if (cat.equals(catFamilia)) {
                fam++;
            } else if (cat.equals(catAmigos)) {
                ami++;
            } else if (cat.equals(catTrabajo)) {
                tra++;
            }
        }

        String distribucion = String.format(
                "Distribución por Categoría:\n\n  %s: %d\n  %s: %d\n  %s: %d",
                catFamilia, fam, catAmigos, ami, catTrabajo, tra
        );
        delegado.txt_distribucionCategoria.setText(distribucion);
    }
}