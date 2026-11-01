package vista;

import controlador.logica_ventana;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ventana extends JFrame {

    public JPanel contentPane, panelContactos, panelEstadisticas;
    public JTabbedPane tabbedPane;
    public JTextField txt_nombres, txt_telefono, txt_email, txt_buscar;
    public JCheckBox chb_favorito;
    public JComboBox<String> cmb_categoria, cmb_idioma;
    public JButton btn_add, btn_modificar, btn_eliminar, btn_limpiar;
    public JTable tbl_contactos;
    public DefaultTableModel modeloTabla;
    public JProgressBar prg_carga;
    public ResourceBundle textos;
    public JLabel lbl_totalContactos;
    public JTextArea txt_distribucionCategoria;

    // Paleta de colores y fuentes
    private final Color colorFondo = new Color(173, 216, 230);
    public final Color colorBotones = new Color(255, 178, 145);
    private final Color colorTextoBotones = Color.WHITE;
    private final Font fuentePrincipal = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font fuenteNegrita = new Font("Segoe UI", Font.BOLD, 14);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(() -> {
            try {
                ventana frame = new ventana(new Locale("es", "ES"));
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ventana(Locale localizacion) {
        this.textos = ResourceBundle.getBundle("textos", localizacion);

        configurarVentana();
        inicializarPanelesPrincipales(localizacion);

        new logica_ventana(this);
    }

    private void configurarVentana() {
        setTitle(textos.getString("app.titulo"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(colorFondo);
        setContentPane(contentPane);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(fuentePrincipal);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    private void inicializarPanelesPrincipales(Locale localizacion) {
        // Pestaña de Contactos
        panelContactos = new JPanel(new BorderLayout(10, 10));
        panelContactos.setBackground(colorFondo);
        panelContactos.add(crearPanelFormulario(localizacion), BorderLayout.NORTH);
        panelContactos.add(crearPanelTabla(), BorderLayout.CENTER);
        panelContactos.add(crearPanelSur(), BorderLayout.SOUTH);
        tabbedPane.addTab(textos.getString("tab.contactos"), null, panelContactos, "");

        // Pestaña de Estadísticas
        panelEstadisticas = crearPanelEstadisticas();
        tabbedPane.addTab(textos.getString("tab.estadisticas"), null, panelEstadisticas, "Métricas");
    }

    private JPanel crearPanelFormulario(Locale localizacion) {
        JPanel panelNorte = new JPanel(new GridBagLayout());
        panelNorte.setBackground(colorFondo);
        panelNorte.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 160)),
                textos.getString("border.datos"), TitledBorder.LEFT, TitledBorder.TOP, fuenteNegrita));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txt_nombres = crearCampoTexto(20);
        txt_telefono = crearCampoTexto(20);
        txt_email = crearCampoTexto(20);

        chb_favorito = new JCheckBox(textos.getString("chk.favorito"));
        chb_favorito.setFont(fuenteNegrita);
        chb_favorito.setBackground(colorFondo);

        cmb_categoria = new JComboBox<>(new String[]{
            textos.getString("cmb.categoria.defecto"),
            textos.getString("cat.familia"),
            textos.getString("cat.amigos"),
            textos.getString("cat.trabajo")
        });
        cmb_categoria.setFont(fuentePrincipal);
        cmb_categoria.setBackground(Color.WHITE);

        cmb_idioma = new JComboBox<>(new String[]{"Español", "English", "Kichwa"});
        cmb_idioma.setFont(fuentePrincipal);
        sincronizarComboIdioma(localizacion);

        // Agregar filas del formulario
        agregarAlGrid(panelNorte, crearEtiqueta(textos.getString("lbl.nombres")), gbc, 0, 0);
        agregarAlGrid(panelNorte, txt_nombres, gbc, 1, 0);

        agregarAlGrid(panelNorte, crearEtiqueta(textos.getString("lbl.telefono")), gbc, 0, 1);
        agregarAlGrid(panelNorte, txt_telefono, gbc, 1, 1);

        agregarAlGrid(panelNorte, crearEtiqueta(textos.getString("lbl.email")), gbc, 0, 2);
        agregarAlGrid(panelNorte, txt_email, gbc, 1, 2);

        agregarAlGrid(panelNorte, chb_favorito, gbc, 0, 3);
        agregarAlGrid(panelNorte, cmb_categoria, gbc, 1, 3);

        agregarAlGrid(panelNorte, cmb_idioma, gbc, 1, 4);

        // Panel de Botones
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 5, 5));
        panelBotones.setBackground(colorFondo);

        btn_add = crearBoton(textos.getString("btn.agregar"), "iconos/add.png");
        btn_modificar = crearBoton(textos.getString("btn.modificar"), "iconos/edit.png");
        btn_eliminar = crearBoton(textos.getString("btn.eliminar"), "iconos/delete.png");
        btn_limpiar = crearBoton(textos.getString("btn.limpiar"), "iconos/clear.png");

        panelBotones.add(btn_add);
        panelBotones.add(btn_modificar);
        panelBotones.add(btn_eliminar);
        panelBotones.add(btn_limpiar);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panelNorte.add(panelBotones, gbc);

        return panelNorte;
    }

    private JScrollPane crearPanelTabla() {
        String[] columnas = {
            textos.getString("tbl.nombre"),
            textos.getString("tbl.telefono"),
            textos.getString("tbl.email"),
            textos.getString("tbl.categoria"),
            textos.getString("tbl.favorito")
        };

        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbl_contactos = new JTable(modeloTabla);
        tbl_contactos.setFont(fuentePrincipal);
        tbl_contactos.setRowHeight(25);
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurarRenderizadoresTabla();

        JScrollPane scrTabla = new JScrollPane(tbl_contactos);
        scrTabla.getViewport().setBackground(Color.WHITE);
        return scrTabla;
    }

    private void configurarRenderizadoresTabla() {
        // Renderizador de encabezado
        tbl_contactos.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(colorBotones);
                c.setForeground(Color.WHITE);
                c.setFont(fuenteNegrita);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
                return c;
            }
        });

        // celdas por categoría
        tbl_contactos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(200, 215, 235));
                } else {
                    int modelRow = table.convertRowIndexToModel(row);
                    String categoria = String.valueOf(table.getModel().getValueAt(modelRow, 3));
                    c.setBackground(obtenerColorCategoria(categoria));
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        });
    }

    private Color obtenerColorCategoria(String categoria) {
        if (categoria.equals(textos.getString("cat.familia"))) {
            return new Color(193, 225, 193);
        } else if (categoria.equals(textos.getString("cat.amigos"))) {
            return new Color(137, 207, 240);
        } else if (categoria.equals(textos.getString("cat.trabajo"))) {
            return new Color(255, 240, 225);
        }
        return Color.WHITE;
    }

    private JPanel crearPanelSur() {
        JPanel panelSur = new JPanel(new BorderLayout(5, 5));
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBusqueda.setBackground(colorFondo);

        txt_buscar = crearCampoTexto(40);
        panelBusqueda.add(crearEtiqueta(textos.getString("lbl.buscar")));
        panelBusqueda.add(txt_buscar);

        prg_carga = new JProgressBar();
        prg_carga.setStringPainted(true);
        prg_carga.setVisible(true);

        panelSur.add(panelBusqueda, BorderLayout.CENTER);
        panelSur.add(prg_carga, BorderLayout.SOUTH);
        return panelSur;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(colorFondo);

        GridBagConstraints gbcEst = new GridBagConstraints();
        gbcEst.insets = new Insets(10, 10, 10, 10);
        gbcEst.gridx = 0;
        gbcEst.anchor = GridBagConstraints.CENTER;

        gbcEst.gridy = 0;
        JLabel lblTitulo = new JLabel("Desarrollado Por: DYLAN TOTOY");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(lblTitulo, gbcEst);

        gbcEst.gridy = 1;
        lbl_totalContactos = new JLabel("Total de contactos: 0");
        lbl_totalContactos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lbl_totalContactos, gbcEst);

        gbcEst.gridy = 2;
        txt_distribucionCategoria = new JTextArea("Distribución por Categoría:\n");
        txt_distribucionCategoria.setFont(fuentePrincipal);
        txt_distribucionCategoria.setBackground(colorFondo);
        txt_distribucionCategoria.setEditable(false);
        panel.add(txt_distribucionCategoria, gbcEst);

        return panel;
    }

    private void sincronizarComboIdioma(Locale localizacion) {
        String lang = localizacion.getLanguage();
        if ("en".equals(lang)) {
            cmb_idioma.setSelectedIndex(1);
        } else if ("qu".equals(lang)) {
            cmb_idioma.setSelectedIndex(2);
        } else {
            cmb_idioma.setSelectedIndex(0);
        }
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(fuenteNegrita);
        return label;
    }

    private JTextField crearCampoTexto(int columnas) {
        JTextField textField = new JTextField(columnas);
        textField.setFont(fuentePrincipal);
        return textField;
    }

    private void agregarAlGrid(JPanel panel, Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    private JButton crearBoton(String texto, String nombreArchivo) {
        JButton boton = new JButton(texto);
        boton.setFont(fuenteNegrita);
        boton.setBackground(colorBotones);
        boton.setForeground(colorTextoBotones);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);

        ImageIcon icono = new ImageIcon("src/" + nombreArchivo);
        if (icono.getIconWidth() == -1) {
            icono = new ImageIcon(nombreArchivo);
        }

        if (icono.getIconWidth() != -1) {
            boton.setIcon(icono);
        } else {
            System.err.println("No se pudo cargar el icono: " + nombreArchivo);
        }
        return boton;
    }
}