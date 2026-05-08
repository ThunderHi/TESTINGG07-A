import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class SistemaAsistencia extends JFrame {

    // --- Constantes globales ---
    private static final String PASS_DOCENTE = "1234567890";
    private static final String PASS_ESTUDIANTE = "72807827";
    private static final String CODIGO_DOCENTE = "1234567890";
    private static final String ARCHIVO_DATOS = "datos_asistencia.dat";
    
    // --- Modelos de Datos ---
    static class Estudiante implements Serializable {
        private static final long serialVersionUID = 1L;

        String id;
        String apellidos;
        String nombres;

        public Estudiante(String id, String apellidos, String nombres) {
            this.id = limpiarTexto(id);
            this.apellidos = limpiarTexto(apellidos);
            this.nombres = limpiarTexto(nombres);
        }
    }

    static class Registro implements Serializable {
        private static final long serialVersionUID = 1L;

        String fecha;
        String estado; // P, T, AI, AJ
        String justificante;
        boolean guardadoPorEstudiante;

        public Registro(String fecha, String estado, String justificante) {
            this.fecha = limpiarFecha(fecha);
            this.estado = limpiarTexto(estado);
            this.justificante = limpiarTexto(justificante);
            this.guardadoPorEstudiante = false;
        }
    }

    static class ConfigFecha implements Serializable {
        private static final long serialVersionUID = 1L;

        String fecha;
        boolean bloqueado;
        boolean habilitadoParaAlumno;
        boolean tieneActividad; // Para el cálculo de porcentaje real

        public ConfigFecha(String fecha) {
            this.fecha = limpiarFecha(fecha);
            this.bloqueado = true; // Siempre bloqueada al inicio
            this.habilitadoParaAlumno = false;
            this.tieneActividad = false;
        }
    }

    static class DatosSistema implements Serializable {
        private static final long serialVersionUID = 1L;

        Map<String, List<Registro>> baseDatosAsistencia;
        List<Estudiante> listaEstudiantes;
        List<ConfigFecha> sesionesConfig;

        public DatosSistema(Map<String, List<Registro>> baseDatosAsistencia,
                            List<Estudiante> listaEstudiantes,
                            List<ConfigFecha> sesionesConfig) {
            this.baseDatosAsistencia = baseDatosAsistencia;
            this.listaEstudiantes = listaEstudiantes;
            this.sesionesConfig = sesionesConfig;
        }
    }

    // --- Base de Datos en Memoria ---
    private static Map<String, List<Registro>> baseDatosAsistencia = new HashMap<>();
    private static List<Estudiante> listaEstudiantes = new ArrayList<>();
    private static List<ConfigFecha> sesionesConfig = new ArrayList<>();

    // Usuario actual
    private String currentUserId;
    private String currentUserRole;
    private String currentUserName;

    public SistemaAsistencia() {
        cargarDatos();
        mostrarLogin();
    }

    private void inicializarDatos() {
        listaEstudiantes.clear();
        baseDatosAsistencia.clear();
        sesionesConfig.clear();

        // Alumnos ordenados alfabéticamente
        listaEstudiantes.add(new Estudiante("2023203021", "Huacallo Inga", "Thunder Jesus"));
        listaEstudiantes.add(new Estudiante("2023803011", "Rojas Luna", "Kevin Jostin"));
        listaEstudiantes.add(new Estudiante("2021601981", "Urbiola Urquizo", "Hugo Raul"));
        Collections.sort(listaEstudiantes, (a, b) -> a.apellidos.compareTo(b.apellidos));

        for (Estudiante e : listaEstudiantes) {
            baseDatosAsistencia.put(e.id, new ArrayList<>());
        }

        // Semillas de sesiones de prueba sugeridas
        String[] pruebas = {"04/05/2026", "05/05/2026", "06/05/2026"};
        for (String p : pruebas) {
            ConfigFecha c = new ConfigFecha(p);
            c.tieneActividad = true; // Las de prueba ya cuentan
            sesionesConfig.add(c);
        }
    }

    private void guardarDatos() {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            DatosSistema datos = new DatosSistema(baseDatosAsistencia, listaEstudiantes, sesionesConfig);
            salida.writeObject(datos);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron guardar los datos: " + e.getMessage(),
                    "Error de guardado",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        File archivo = new File(ARCHIVO_DATOS);

        if (!archivo.exists()) {
            inicializarDatos();
            guardarDatos();
            return;
        }

        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            DatosSistema datos = (DatosSistema) entrada.readObject();
            baseDatosAsistencia = datos.baseDatosAsistencia;
            listaEstudiantes = datos.listaEstudiantes;
            sesionesConfig = datos.sesionesConfig;
            verificarEstructuraDatos();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los datos guardados. Se iniciará con datos base.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            inicializarDatos();
            guardarDatos();
        }
    }

    private void verificarEstructuraDatos() {
        if (baseDatosAsistencia == null) baseDatosAsistencia = new HashMap<>();
        if (listaEstudiantes == null) listaEstudiantes = new ArrayList<>();
        if (sesionesConfig == null) sesionesConfig = new ArrayList<>();

        for (Estudiante e : listaEstudiantes) {
            e.id = limpiarTexto(e.id);
            e.apellidos = limpiarTexto(e.apellidos);
            e.nombres = limpiarTexto(e.nombres);
            baseDatosAsistencia.putIfAbsent(e.id, new ArrayList<>());
        }

        for (ConfigFecha c : sesionesConfig) {
            c.fecha = limpiarFecha(c.fecha);
        }

        for (List<Registro> registros : baseDatosAsistencia.values()) {
            for (Registro r : registros) {
                r.fecha = limpiarFecha(r.fecha);
                r.estado = limpiarTexto(r.estado);
                r.justificante = limpiarTexto(r.justificante);
            }
        }

        Collections.sort(listaEstudiantes, (a, b) -> a.apellidos.compareTo(b.apellidos));
    }

    // --- LOGIN ---
    private void mostrarLogin() {
        setTitle("ACCESO - GRUPO 07 ASISTENCIA");
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("ACCESO SISTEMA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JButton btnLogin = new JButton("INGRESAR");
        btnLogin.setBackground(new Color(79, 70, 229));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        
        gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel l1 = new JLabel("Código:"); l1.setForeground(Color.GRAY);
        panel.add(l1, gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel l2 = new JLabel("Password:"); l2.setForeground(Color.GRAY);
        panel.add(l2, gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        setContentPane(panel);

        btnLogin.addActionListener(e -> {
            String id = limpiarTexto(txtUser.getText());
            String pass = limpiarTexto(new String(txtPass.getPassword()));

            // Validación: exactamente 10 dígitos
            if (!id.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "El código debe tener exactamente 10 dígitos.");
                return;
            }

            if (id.equals(CODIGO_DOCENTE) && pass.equals(PASS_DOCENTE)) {
                currentUserId = id; currentUserRole = "DOCENTE"; currentUserName = "Edmundo Gonzales";
                mostrarVentanaDocente();
            } else {
                Estudiante st = listaEstudiantes.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null);
                if (st != null && pass.equals(PASS_ESTUDIANTE)) {
                    currentUserId = id; currentUserRole = "ESTUDIANTE"; currentUserName = st.nombres + " " + st.apellidos;
                    mostrarVentanaEstudiante();
                } else {
                    JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    // --- VENTANA DOCENTE ---
    private void mostrarVentanaDocente() {
        setTitle("Panel Docente - " + currentUserName);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        getContentPane().removeAll();

        JTabbedPane tabs = new JTabbedPane();

        // TAB 1: Registro Diario
        JPanel pnlReg = new JPanel(new BorderLayout());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JComboBox<String> cbFechas = new JComboBox<>();
        actualizarComboFechas(cbFechas);
        
        JButton btnAdd = new JButton("+ Fecha");
        JButton btnDel = new JButton("Eliminar");
        JToggleButton tglLock = new JToggleButton("Desbloquear");
        JCheckBox chkEscritura = new JCheckBox("Habilitar Marcado Alumno");
        JButton btnCerrarSesionDocente = new JButton("Cerrar sesión");
        
        toolbar.add(new JLabel("Sesión:")); toolbar.add(cbFechas);
        toolbar.add(btnAdd); toolbar.add(btnDel); toolbar.add(tglLock); toolbar.add(chkEscritura);
        toolbar.add(btnCerrarSesionDocente);

        String[] cols = {"Código", "Alumno", "Estado Actual", "Nota"};
        DefaultTableModel modReg = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modReg);
        tabla.setRowHeight(30);

        pnlReg.add(toolbar, BorderLayout.NORTH);
        pnlReg.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pnlAcc = new JPanel(new FlowLayout());
        JButton bP = new JButton("Presente");
        JButton bT = new JButton("Tardanza");
        JButton bAI = new JButton("Ausencia Injustificada");
        JButton bAJ = new JButton("Ausencia Justificada");
        pnlAcc.add(bP); pnlAcc.add(bT); pnlAcc.add(bAI); pnlAcc.add(bAJ);
        pnlReg.add(pnlAcc, BorderLayout.SOUTH);

        // TAB 2: Estadísticas
        JPanel pnlStats = new JPanel(new BorderLayout());
        String[] colS = {"ID", "Estudiante", "Presente", "Tardanza", "Aus. Inj", "Aus. Jus", "% Total"};
        DefaultTableModel modStats = new DefaultTableModel(colS, 0);
        JTable tabS = new JTable(modStats);
        JButton btnExp = new JButton("Descargar Reporte Detallado (.CSV)");
        pnlStats.add(new JScrollPane(tabS), BorderLayout.CENTER);
        pnlStats.add(btnExp, BorderLayout.SOUTH);

        // -- LÓGICA DOCENTE --
        btnAdd.addActionListener(e -> {
            String f = (String) JOptionPane.showInputDialog(this, "Fecha dd/mm/aaaa:", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            
            if (f == null) {
                return; // Canceló la ventana
            }
            
            f = limpiarFecha(f);
            
            if (f.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar una fecha.", "Fecha requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!esFechaValida(f)) {
                JOptionPane.showMessageDialog(this, "Formato inválido. Ingrese la fecha exactamente como dd/mm/aaaa.\nEjemplo válido: 19/10/2026", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (buscarConfig(f) != null) {
                JOptionPane.showMessageDialog(this, "La fecha ya existe en el sistema.", "Fecha duplicada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            ConfigFecha nc = new ConfigFecha(f);
            sesionesConfig.add(nc);
            guardarDatos();
            actualizarComboFechas(cbFechas);
            cbFechas.setSelectedItem(f);
            JOptionPane.showMessageDialog(this, "Fecha agregada correctamente.");
        });

        btnDel.addActionListener(e -> {
            String f = limpiarFecha((String) cbFechas.getSelectedItem());
            if (f == null || f.isEmpty()) return;
            int c = JOptionPane.showConfirmDialog(this, "¿Seguro quiere eliminar la sesión y todas sus asistencias relacionadas?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                sesionesConfig.removeIf(cf -> limpiarFecha(cf.fecha).equals(f));
                eliminarAsistenciasPorFecha(f);
                guardarDatos();
                actualizarComboFechas(cbFechas);
                refrescarTablaDocente(modReg, (String) cbFechas.getSelectedItem());
            }
        });

        tglLock.addActionListener(e -> {
            String f = limpiarFecha((String) cbFechas.getSelectedItem());
            ConfigFecha conf = buscarConfig(f);
            if (conf != null) {
                conf.bloqueado = !tglLock.isSelected();
                tglLock.setText(conf.bloqueado ? "Desbloquear" : "Bloquear (Cerrar)");
                
                // Si bloquea, auto-marcar AI a los que no tienen nada
                if (conf.bloqueado) {
                    conf.tieneActividad = true;
                    for (Estudiante est : listaEstudiantes) {
                        Registro r = buscarReg(est.id, f);
                        if (r == null) {
                            actualizarRegManual(est.id, f, "AI", "auto-marcado");
                        }
                    }
                    guardarDatos();
                    refrescarTablaDocente(modReg, f);
                    JOptionPane.showMessageDialog(this, "Fecha cerrada. Vacíos marcados como Ausencia Injustificada.");
                } else {
                    guardarDatos();
                }
            }
        });

        chkEscritura.addActionListener(e -> {
            ConfigFecha conf = buscarConfig((String) cbFechas.getSelectedItem());
            if (conf != null) {
                conf.habilitadoParaAlumno = chkEscritura.isSelected();
                guardarDatos();
            }
        });

        cbFechas.addActionListener(e -> {
            String f = limpiarFecha((String) cbFechas.getSelectedItem());
            ConfigFecha cf = buscarConfig(f);
            if (cf != null) {
                tglLock.setSelected(!cf.bloqueado);
                tglLock.setText(cf.bloqueado ? "Desbloquear" : "Bloquear (Cerrar)");
                chkEscritura.setSelected(cf.habilitadoParaAlumno);
                refrescarTablaDocente(modReg, f);
            }
        });

        ActionListener markL = al -> {
            String f = limpiarFecha((String) cbFechas.getSelectedItem());
            ConfigFecha cf = buscarConfig(f);
            if (cf == null || cf.bloqueado) {
                JOptionPane.showMessageDialog(this, "Edición bloqueada para esta fecha."); return;
            }
            int row = tabla.getSelectedRow();
            if (row == -1) return;
            String sid = limpiarTexto((String) modReg.getValueAt(row, 0));
            String est = ""; String just = "-";

            if (al.getSource() == bP) est = "P";
            else if (al.getSource() == bT) est = "T";
            else if (al.getSource() == bAI) est = "AI";
            else if (al.getSource() == bAJ) {
                est = "AJ";
                Object[] op = {"Salud", "Familia", "Trabajo", "Otro", "Justificado"};
                just = (String) JOptionPane.showInputDialog(this, "Motivo:", "AJ", 3, null, op, op[4]);
                if (just == null) return;
                just = limpiarTexto(just);
            }

            // Permitir deseleccionar si marca lo mismo
            Registro actual = buscarReg(sid, f);
            if (actual != null && actual.estado.equals(est)) {
                baseDatosAsistencia.get(sid).remove(actual);
            } else {
                actualizarRegManual(sid, f, est, just);
                cf.tieneActividad = true;
            }
            guardarDatos();
            refrescarTablaDocente(modReg, f);
        };

        bP.addActionListener(markL); bT.addActionListener(markL); bAI.addActionListener(markL); bAJ.addActionListener(markL);
        btnExp.addActionListener(e -> exportarCSV());
        btnCerrarSesionDocente.addActionListener(e -> cerrarSesion());

        tabs.addTab("Control de Asistencia", pnlReg);
        tabs.addTab("Estadísticas de Curso", pnlStats);
        tabs.addChangeListener(e -> { if(tabs.getSelectedIndex() == 1) refrescarStats(modStats); });

        add(tabs); revalidate(); repaint();
    }

    private void refrescarTablaDocente(DefaultTableModel mod, String f) {
        f = limpiarFecha(f);
        mod.setRowCount(0);
        if (f == null || f.isEmpty()) return;
        for (Estudiante e : listaEstudiantes) {
            Registro r = buscarReg(e.id, f);
            String st = (r == null) ? "Sin marcar" : r.estado;
            String obs = (r == null) ? "-" : r.justificante;
            mod.addRow(new Object[]{e.id, e.apellidos + ", " + e.nombres, st, obs});
        }
    }

    private void refrescarStats(DefaultTableModel mod) {
        mod.setRowCount(0);
        long habilitadas = sesionesConfig.stream().filter(c -> c.tieneActividad).count();
        for (Estudiante e : listaEstudiantes) {
            int p=0, t=0, ai=0, aj=0;
            for (Registro r : baseDatosAsistencia.get(e.id)) {
                ConfigFecha config = buscarConfig(r.fecha);
                if (config != null && config.tieneActividad) {
                    switch(r.estado) {
                        case "P": p++; break;
                        case "T": t++; break;
                        case "AI": ai++; break;
                        case "AJ": aj++; break;
                    }
                }
            }
            ai += (habilitadas - (p + t + ai + aj));
            double perc = (habilitadas == 0) ? 0 : ((double)(p + t + aj) / habilitadas) * 100;
            mod.addRow(new Object[]{e.id, e.apellidos, p, t, ai, aj, String.format("%.2f%%", perc)});
        }
    }

    // --- VENTANA ESTUDIANTE ---
    private void mostrarVentanaEstudiante() {
        setTitle("Panel Alumno - Grupo 07 - " + currentUserName);
        setSize(900, 620);
        setLocationRelativeTo(null);
        getContentPane().removeAll();

        JPanel pnl = new JPanel(new BorderLayout(15, 15));
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior: marcado y cierre de sesión
        JPanel pnlSuperior = new JPanel(new BorderLayout());
        JPanel pnlMark = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCerrarSesionAlumno = new JButton("Cerrar sesión");
        JComboBox<String> cbMark = new JComboBox<>();
        actualizarCBAlumno(cbMark);
        
        String[] opts = {"Elija...", "Presente", "Tardanza", "Ausencia Justificada"};
        JComboBox<String> cbEst = new JComboBox<>(opts);
        JButton btnSave = new JButton("GUARDAR ASISTENCIA");
        
        pnlMark.add(new JLabel("Sesión Activa:")); pnlMark.add(cbMark);
        pnlMark.add(new JLabel("Mi estado:")); pnlMark.add(cbEst);
        pnlMark.add(btnSave);
        pnlSuperior.add(pnlMark, BorderLayout.CENTER);
        pnlSuperior.add(btnCerrarSesionAlumno, BorderLayout.EAST);

        // Resumen del alumno
        JPanel pnlResumen = new JPanel(new GridLayout(1, 6, 8, 8));
        JLabel lblTotal = crearEtiquetaResumen("Registros: 0");
        JLabel lblP = crearEtiquetaResumen("P: 0");
        JLabel lblT = crearEtiquetaResumen("T: 0");
        JLabel lblAI = crearEtiquetaResumen("AI: 0");
        JLabel lblAJ = crearEtiquetaResumen("AJ: 0");
        JLabel lblPorcentaje = crearEtiquetaResumen("Asistencia: 0.00%");
        pnlResumen.add(lblTotal);
        pnlResumen.add(lblP);
        pnlResumen.add(lblT);
        pnlResumen.add(lblAI);
        pnlResumen.add(lblAJ);
        pnlResumen.add(lblPorcentaje);

        // Tabla Historial (Orden Inverso)
        String[] headers = {"Fecha de Sesión", "Mi Marcado", "Observaciones / Nota"};
        DefaultTableModel modH = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabH = new JTable(modH);
        tabH.setRowHeight(28);

        // Panel de estadísticas detalladas
        String[] headersStats = {"Indicador", "Cantidad"};
        DefaultTableModel modStatsAlumno = new DefaultTableModel(headersStats, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabStatsAlumno = new JTable(modStatsAlumno);

        JTabbedPane tabsAlumno = new JTabbedPane();
        tabsAlumno.addTab("Mi historial", new JScrollPane(tabH));
        tabsAlumno.addTab("Mis estadísticas", new JScrollPane(tabStatsAlumno));

        JButton btnRep = new JButton("Descargar Mi Reporte Personal");

        JPanel pnlCentro = new JPanel(new BorderLayout(10, 10));
        pnlCentro.add(pnlResumen, BorderLayout.NORTH);
        pnlCentro.add(tabsAlumno, BorderLayout.CENTER);

        pnl.add(pnlSuperior, BorderLayout.NORTH);
        pnl.add(pnlCentro, BorderLayout.CENTER);
        pnl.add(btnRep, BorderLayout.SOUTH);

        Runnable refrescarVistaAlumno = () -> {
            refrescarHistorialAlumno(modH);
            refrescarStatsAlumno(modStatsAlumno, lblTotal, lblP, lblT, lblAI, lblAJ, lblPorcentaje);
        };
        refrescarVistaAlumno.run();

        btnSave.addActionListener(e -> {
            String f = limpiarFecha((String) cbMark.getSelectedItem());
            String res = limpiarTexto((String) cbEst.getSelectedItem());
            if (f == null || f.isEmpty() || res.equals("Elija...")) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar la sesión y su estado."); return;
            }
            
            ConfigFecha cf = buscarConfig(f);
            if (cf == null || cf.bloqueado || !cf.habilitadoParaAlumno) {
                JOptionPane.showMessageDialog(this, "La sesión se ha cerrado."); return;
            }

            if (buscarReg(currentUserId, f) != null) {
                JOptionPane.showMessageDialog(this, "Ya registró asistencia para esta sesión.");
                actualizarCBAlumno(cbMark);
                refrescarVistaAlumno.run();
                return;
            }

            int conf = JOptionPane.showConfirmDialog(this, "¿Desea guardar? Verifique antes de confirmar, no podrá editarlo luego.", "Guardar Asistencia", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                String cod = res.equals("Presente") ? "P" : res.equals("Tardanza") ? "T" : "AJ";
                String obs = cod.equals("AJ") ? "por justificar" : "-";
                
                Registro r = new Registro(f, cod, obs);
                r.guardadoPorEstudiante = true;
                baseDatosAsistencia.get(currentUserId).removeIf(reg -> limpiarFecha(reg.fecha).equals(f));
                baseDatosAsistencia.get(currentUserId).add(r);
                cf.tieneActividad = true;
                guardarDatos();

                if (cod.equals("AJ")) {
                    JOptionPane.showMessageDialog(this, "Debe presentar su justificación en la siguiente clase.");
                } else {
                    JOptionPane.showMessageDialog(this, "¡Asistencia Guardada Satisfactoriamente!");
                }
                cbEst.setSelectedIndex(0);
                actualizarCBAlumno(cbMark);
                refrescarVistaAlumno.run();
            }
        });

        btnRep.addActionListener(e -> exportarCSVAlumno());
        btnCerrarSesionAlumno.addActionListener(e -> cerrarSesion());

        add(pnl); revalidate(); repaint();
    }

    private JLabel crearEtiquetaResumen(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(245, 245, 245));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private void refrescarStatsAlumno(DefaultTableModel mod, JLabel lblTotal, JLabel lblP, JLabel lblT, JLabel lblAI, JLabel lblAJ, JLabel lblPorcentaje) {
        int p = 0, t = 0, ai = 0, aj = 0;
        long sesionesConActividad = sesionesConfig.stream().filter(c -> c.tieneActividad).count();

        for (ConfigFecha cf : sesionesConfig) {
            if (!cf.tieneActividad) continue;
            Registro r = buscarReg(currentUserId, cf.fecha);
            String estado = (r == null) ? "AI" : r.estado;
            switch (estado) {
                case "P": p++; break;
                case "T": t++; break;
                case "AJ": aj++; break;
                default: ai++; break;
            }
        }

        int totalRegistros = p + t + ai + aj;
        double porcentaje = sesionesConActividad == 0 ? 0 : ((double) (p + t + aj) / sesionesConActividad) * 100;

        lblTotal.setText("Registros: " + totalRegistros);
        lblP.setText("P: " + p);
        lblT.setText("T: " + t);
        lblAI.setText("AI: " + ai);
        lblAJ.setText("AJ: " + aj);
        lblPorcentaje.setText(String.format("Asistencia: %.2f%%", porcentaje));

        mod.setRowCount(0);
        mod.addRow(new Object[]{"Sesiones consideradas", sesionesConActividad});
        mod.addRow(new Object[]{"Presente", p});
        mod.addRow(new Object[]{"Tardanza", t});
        mod.addRow(new Object[]{"Ausencia injustificada", ai});
        mod.addRow(new Object[]{"Ausencia justificada", aj});
        mod.addRow(new Object[]{"Porcentaje de asistencia", String.format("%.2f%%", porcentaje)});
    }

    private void refrescarHistorialAlumno(DefaultTableModel mod) {
        mod.setRowCount(0);
        List<ConfigFecha> reversa = new ArrayList<>(sesionesConfig);
        Collections.reverse(reversa);
        for (ConfigFecha f : reversa) {
            Registro r = buscarReg(currentUserId, f.fecha);
            String st = (r == null) ? (f.bloqueado ? "AI" : "Pendiente") : r.estado;
            String nt = (r == null) ? "-" : r.justificante;
            mod.addRow(new Object[]{f.fecha, st, nt});
        }
    }

    private void actualizarCBAlumno(JComboBox<String> cb) {
        cb.removeAllItems();
        for (ConfigFecha f : sesionesConfig) {
            if (f.habilitadoParaAlumno && !f.bloqueado) {
                // Si el alumno ya marcó para esta fecha, no mostrarla en el selector
                if (buscarReg(currentUserId, f.fecha) == null) cb.addItem(f.fecha);
            }
        }
    }

    // --- MÉTODOS DE SOPORTE ---
    private boolean esFechaValida(String fecha) {
        fecha = limpiarFecha(fecha);

        // Primero se valida la forma exacta: 2 dígitos / 2 dígitos / 4 dígitos
        if (!fecha.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false); // Evita fechas como 32/13/2026
        try {
            formato.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this, "¿Desea cerrar sesión?", "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            currentUserId = null;
            currentUserRole = null;
            currentUserName = null;
            getContentPane().removeAll();
            mostrarLogin();
            revalidate();
            repaint();
        }
    }

    private ConfigFecha buscarConfig(String f) {
        f = limpiarFecha(f);
        if (f == null || f.isEmpty()) return null;
        final String fechaBuscada = f;
        return sesionesConfig.stream().filter(c -> limpiarFecha(c.fecha).equals(fechaBuscada)).findFirst().orElse(null);
    }
    
    private Registro buscarReg(String sid, String f) {
        sid = limpiarTexto(sid);
        f = limpiarFecha(f);
        List<Registro> rs = baseDatosAsistencia.get(sid);
        if (rs == null || f == null || f.isEmpty()) return null;
        final String fechaBuscada = f;
        return rs.stream().filter(r -> limpiarFecha(r.fecha).equals(fechaBuscada)).findFirst().orElse(null);
    }

    private void actualizarRegManual(String sid, String f, String est, String j) {
        sid = limpiarTexto(sid);
        f = limpiarFecha(f);
        est = limpiarTexto(est);
        j = limpiarTexto(j);

        List<Registro> rs = baseDatosAsistencia.get(sid);
        if (rs == null) {
            rs = new ArrayList<>();
            baseDatosAsistencia.put(sid, rs);
        }

        final String fechaBuscada = f;
        rs.removeIf(r -> limpiarFecha(r.fecha).equals(fechaBuscada));
        rs.add(new Registro(f, est, j));
    }

    private void eliminarAsistenciasPorFecha(String f) {
        f = limpiarFecha(f);
        final String fechaBuscada = f;
        for (List<Registro> registros : baseDatosAsistencia.values()) {
            registros.removeIf(r -> limpiarFecha(r.fecha).equals(fechaBuscada));
        }
    }

    private void actualizarComboFechas(JComboBox<String> cb) {
        cb.removeAllItems();
        for (ConfigFecha cf : sesionesConfig) cb.addItem(limpiarFecha(cf.fecha));
    }

    private static String limpiarFecha(String fecha) {
        return limpiarTexto(fecha).replace(" ", "");
    }

    private static String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim()
                .replaceAll("[\\n\\t\\r]", " ")
                .replaceAll("\\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String limpiarCSV(String texto) {
        texto = limpiarTexto(texto);
        texto = texto.replace("\"", "\"\"");
        return "\"" + texto + "\"";
    }

    private void exportarCSV() {
        try (FileWriter fw = new FileWriter("Reporte_Docente_G07.csv")) {
            fw.write(limpiarCSV("CURSO: TESTING DE SISTEMAS") + "," + limpiarCSV("DOCENTE: EDMUNDO GONZALES") + "\n");
            fw.write("ID,Alumno,Presente,Tardanza,Aus.Inj,Aus.Jus,%\n");
            long habilitadas = sesionesConfig.stream().filter(c -> c.tieneActividad).count();
            for (Estudiante e : listaEstudiantes) {
                int p=0, t=0, ai=0, aj=0;
                for (Registro r : baseDatosAsistencia.get(e.id)) {
                    ConfigFecha config = buscarConfig(r.fecha);
                    if (config != null && config.tieneActividad) {
                        if (r.estado.equals("P")) p++;
                        else if (r.estado.equals("T")) t++;
                        else if (r.estado.equals("AI")) ai++;
                        else if (r.estado.equals("AJ")) aj++;
                    }
                }
                ai += (habilitadas - (p+t+ai+aj));
                double perc = (habilitadas == 0)?0:((double)(p+t+aj)/habilitadas)*100;
                fw.write(limpiarCSV(e.id) + "," + limpiarCSV(e.apellidos + " " + e.nombres) + "," + p + "," + t + "," + ai + "," + aj + "," + limpiarCSV(String.format("%.2f%%", perc)) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Archivo 'Reporte_Docente_G07.csv' generado.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void exportarCSVAlumno() {
        try (FileWriter fw = new FileWriter("Reporte_" + currentUserId + ".csv")) {
            fw.write(limpiarCSV("CURSO: TESTING DE SISTEMAS") + "," + limpiarCSV("DOCENTE: EDMUNDO GONZALES") + "\n");
            fw.write(limpiarCSV("ESTUDIANTE: " + currentUserName + " (" + currentUserId + ")") + "\n\n");
            fw.write("Sesión,Mi Estado,Nota\n");
            for (ConfigFecha c : sesionesConfig) {
                Registro r = buscarReg(currentUserId, c.fecha);
                String st = (r == null) ? "AI" : r.estado;
                String nt = (r == null) ? "Falta" : r.justificante;
                fw.write(limpiarCSV(c.fecha) + "," + limpiarCSV(st) + "," + limpiarCSV(nt) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Tu reporte 'Reporte_" + currentUserId + ".csv' ha sido generado.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        new SistemaAsistencia();
    }
}
