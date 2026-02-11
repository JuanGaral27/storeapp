import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.ArrayList;

public class CRUD {
    static Connection conexion;
    static String tablaActual;

    // Colores Profesionales
    static Color colorInicio = new Color(236, 240, 241); // Gris muy claro
    static Color colorFin = new Color(189, 195, 199);    // Gris plata
    static Color colorPrimario = new Color(41, 128, 185); 
    static Color colorTexto = new Color(44, 62, 80);

    // Clase para el fondo profesional con degradado
    static class PanelDegradado extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, colorInicio, 0, getHeight(), colorFin);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Acceso Profesional");
        f.setSize(380, 520);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);

        PanelDegradado pMain = new PanelDegradado();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBorder(new EmptyBorder(40, 45, 40, 45));

        JLabel lblLogin = new JLabel("SISTEMA CRUD");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogin.setForeground(colorTexto);
        lblLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Campos de texto elegantes (solo línea inferior)
        JTextField txtBD = crearCampoRefinado("Base de Datos", "PV");
        JTextField txtUser = crearCampoRefinado("Usuario", "postgres");
        JPasswordField txtPass = new JPasswordField();
        estilizarComponente(txtPass);
        JTextField txtTabla = crearCampoRefinado("Tabla", "productos");

        pMain.add(lblLogin); pMain.add(Box.createVerticalStrut(30));
        pMain.add(lblGuia("Base de Datos")); pMain.add(txtBD); pMain.add(Box.createVerticalStrut(15));
        pMain.add(lblGuia("Usuario")); pMain.add(txtUser); pMain.add(Box.createVerticalStrut(15));
        pMain.add(lblGuia("Contraseña")); pMain.add(txtPass); pMain.add(Box.createVerticalStrut(15));
        pMain.add(lblGuia("Tabla")); pMain.add(txtTabla);

        JButton btnLogin = new JButton("CONECTAR");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(220, 45));
        btnLogin.setBackground(colorPrimario);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pMain.add(Box.createVerticalStrut(30));
        pMain.add(btnLogin);

        f.add(pMain);
        btnLogin.addActionListener(e -> {
            try {
                String url = "jdbc:postgresql://localhost:5432/" + txtBD.getText();
                tablaActual = txtTabla.getText();
                conexion = DriverManager.getConnection(url, txtUser.getText(), new String(txtPass.getPassword()));
                f.dispose();
                mostrarCRUD();
            } catch (Exception ex) { JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage()); }
        });
        f.setVisible(true);
    }

    static void mostrarCRUD() {
        JFrame v = new JFrame("Panel Control - " + tablaActual.toUpperCase());
        v.setSize(1100, 700);
        v.setLocationRelativeTo(null);

        PanelDegradado pFondo = new PanelDegradado();
        pFondo.setLayout(new BorderLayout());
        v.setContentPane(pFondo);

        DefaultTableModel modelo = new DefaultTableModel() { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        JTable tabla = new JTable(modelo);
        
        // Estilo de Tabla
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setSelectionBackground(new Color(174, 182, 191));
        
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(44, 62, 80));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        cargarTabla(modelo, "");

        // Panel Superior con botones y búsqueda
        JPanel pTop = new JPanel(new BorderLayout(20, 0));
        pTop.setOpaque(false); // Para que se vea el degradado de fondo
        pTop.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pBotones.setOpaque(false);
        JButton bAdd = botonElegante("Nuevo", new Color(46, 204, 113));
        JButton bEdit = botonElegante("Editar", new Color(241, 196, 15));
        JButton bDel = botonElegante("Eliminar", new Color(231, 76, 60));
        pBotones.add(bAdd); pBotones.add(bEdit); pBotones.add(bDel);

        // BUSCADOR FILTRADO POR ID Y NOMBRE
        JTextField txtBus = new JTextField(18);
        txtBus.setBorder(new MatteBorder(0, 0, 2, 0, colorPrimario));
        txtBus.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        txtBus.setOpaque(false);
        
        JPanel pBus = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBus.setOpaque(false);
        JLabel lblLupa = new JLabel("🔍 Filtrar (ID/Nombre): ");
        lblLupa.setForeground(colorTexto);
        pBus.add(lblLupa); pBus.add(txtBus);

        txtBus.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { cargarTabla(modelo, txtBus.getText()); }
        });

        bAdd.addActionListener(e -> formulario(modelo, null));
        bEdit.addActionListener(e -> {
            int f = tabla.getSelectedRow();
            if (f != -1) formulario(modelo, modelo.getValueAt(f, 0));
            else JOptionPane.showMessageDialog(null, "Selecciona una fila");
        });
        bDel.addActionListener(e -> eliminar(tabla, modelo));

        pTop.add(pBotones, BorderLayout.WEST);
        pTop.add(pBus, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new EmptyBorder(0, 25, 25, 25));
        scroll.getViewport().setBackground(Color.WHITE);

        v.add(pTop, BorderLayout.NORTH);
        v.add(scroll, BorderLayout.CENTER);
        v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        v.setVisible(true);
    }

    // --- FUNCIONES DE ESTILO ---
    private static JLabel lblGuia(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(127, 140, 141));
        return l;
    }

    private static JTextField crearCampoRefinado(String t, String v) {
        JTextField tf = new JTextField(v);
        estilizarComponente(tf);
        return tf;
    }

    private static void estilizarComponente(JComponent c) {
        c.setPreferredSize(new Dimension(250, 30));
        c.setBorder(new MatteBorder(0, 0, 1, 0, new Color(149, 165, 166)));
        c.setOpaque(false);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    }

    private static JButton botonElegante(String t, Color c) {
        JButton b = new JButton(t.toUpperCase());
        b.setPreferredSize(new Dimension(110, 38));
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // --- LÓGICA DE DATOS ---
    static void cargarTabla(DefaultTableModel modelo, String filtro) {
        try {
            String sql = "SELECT * FROM " + tablaActual;
            if (!filtro.isEmpty()) sql += " WHERE CAST(id AS TEXT) ILIKE ? OR nombre ILIKE ?";
            sql += " ORDER BY id ASC";

            PreparedStatement ps = conexion.prepareStatement(sql);
            if (!filtro.isEmpty()) {
                ps.setString(1, "%" + filtro + "%");
                ps.setString(2, "%" + filtro + "%");
            }

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int n = meta.getColumnCount();

            Object[] cab = new Object[n];
            for (int i = 1; i <= n; i++) cab[i-1] = meta.getColumnName(i).toUpperCase();
            modelo.setColumnIdentifiers(cab);
            modelo.setRowCount(0);

            while (rs.next()) {
                Object[] fila = new Object[n];
                for (int i = 1; i <= n; i++) fila[i-1] = rs.getObject(i);
                modelo.addRow(fila);
            }
        } catch (Exception e) { }
    }

    static void formulario(DefaultTableModel modelo, Object idEdit) {
        try {
            ResultSet rsMeta = conexion.createStatement().executeQuery("SELECT * FROM " + tablaActual + " LIMIT 1");
            ResultSetMetaData meta = rsMeta.getMetaData();
            JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
            p.setBackground(Color.WHITE);
            ArrayList<JTextField> campos = new ArrayList<>();
            ArrayList<String> nombres = new ArrayList<>();
            ArrayList<String> tipos = new ArrayList<>();

            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String col = meta.getColumnName(i);
                if (i == 1 || col.equalsIgnoreCase("fecha_creacion")) continue; 
                p.add(new JLabel(col.toUpperCase()));
                JTextField tf = new JTextField();
                tf.setBorder(new MatteBorder(0,0,1,0, Color.GRAY));
                if (idEdit != null) {
                    ResultSet rsVal = conexion.createStatement().executeQuery("SELECT "+col+" FROM "+tablaActual+" WHERE id="+idEdit);
                    if(rsVal.next()) tf.setText(rsVal.getString(1));
                }
                campos.add(tf); nombres.add(col); tipos.add(meta.getColumnTypeName(i).toLowerCase());
                p.add(tf);
            }

         if (JOptionPane.showConfirmDialog(null, p, idEdit == null ? "Nuevo" : "Editar", 2, JOptionPane.PLAIN_MESSAGE) == 0) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < nombres.size(); i++) {
        sb.append("?");
        if (i < nombres.size() - 1) sb.append(",");
    }
    String interrogantes = sb.toString();

    String sql = idEdit == null ? 
        "INSERT INTO " + tablaActual + " (" + String.join(",", nombres) + ") VALUES (" + interrogantes + ")" :
        "UPDATE " + tablaActual + " SET " + String.join("=?,", nombres) + "=? WHERE id=" + idEdit;

    PreparedStatement ps = conexion.prepareStatement(sql);
    for (int i = 0; i < campos.size(); i++) {
        String val = campos.get(i).getText().trim();
        String t = tipos.get(i);
        if (t.contains("int") || t.contains("serial")) {
            ps.setInt(i + 1, val.isEmpty() ? 0 : Integer.parseInt(val.replaceAll("[^0-9]", "")));
        } else if (t.contains("numeric") || t.contains("decimal")) {
            String l = val.replace(",", ".").replaceAll("[^0-9.]", "");
            ps.setBigDecimal(i + 1, l.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(l));
        } else {
            ps.setObject(i + 1, val.isEmpty() ? null : val);
        }
    }
    ps.executeUpdate(); 
    cargarTabla(modelo, "");
}
        } catch (Exception e) { }
    }

    static void eliminar(JTable t, DefaultTableModel m) {
        int f = t.getSelectedRow();
        if (f == -1) return;
        Object id = m.getValueAt(f, 0);
        if (JOptionPane.showConfirmDialog(null, "¿Eliminar ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == 0) {
            try {
                conexion.createStatement().executeUpdate("DELETE FROM "+tablaActual+" WHERE id = " + id);
                cargarTabla(m, "");
            } catch (Exception e) { }
        }
    }
}