import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class SistemaAsistencia extends JFrame {

    // --- Constantes globales ---
    private static final String PASS_DOCENTE = "1234567890";
    private static final String CODIGO_DOCENTE = "1234567890";
    private static final String ARCHIVO_DATOS = "datos_asistencia.dat";
    
    // Colores premium para los botones de bloqueo (UX-03)
    private static final Color COLOR_BLOQUEADO = new Color(220, 38, 38);
    private static final Color COLOR_DESBLOQUEADO = new Color(22, 163, 74);

    // --- Modelos de Datos ---
    static class Estudiante implements Serializable {
        private static final long serialVersionUID = 1L;

        String id;
        String apellidos;
        String nombres;
        String password; // INC-01

        public Estudiante(String id, String apellidos, String nombres, String password) {
            this.id = limpiarTexto(id);
            this.apellidos = limpiarTexto(apellidos);
            this.nombres = limpiarTexto(nombres);
            this.password = limpiarTexto(password);
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

    // --- Resumen de Asistencia para Cálculos Centralizados (BUG-04, UX-05) ---
    static class ResumenAsistencia {
        int presentes = 0;
        int tardanzas = 0;
        int ausenciasInjustificadas = 0;
        int ausenciasJustificadas = 0;
        int sesiones = 0;

        public double porcentaje() {
            if (sesiones == 0) return 0;
            return ((double) (presentes + tardanzas + ausenciasJustificadas) / sesiones) * 100;
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
        
        // UX-06: Confirmar salida y guardar datos
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int opcion = JOptionPane.showConfirmDialog(
                        SistemaAsistencia.this,
                        "¿Desea salir del sistema? Se guardarán los cambios.",
                        "Confirmar salida",
                        JOptionPane.YES_NO_OPTION
                );
                if (opcion == JOptionPane.YES_OPTION) {
                    guardarDatos();
                    dispose();
                    System.exit(0);
                }
            }
        });

        mostrarLogin();
    }

    private void inicializarDatos() {
        listaEstudiantes.clear();
        baseDatosAsistencia.clear();
        sesionesConfig.clear();

        // Alumnos con sus contraseñas individuales (INC-01, INC-04)
        listaEstudiantes.add(new Estudiante("2023203021", "Huacallo Inga", "Thunder Jesus", "72807827"));
        listaEstudiantes.add(new Estudiante("2023803011", "Rojas Luna", "Kevin Jostin", "Rojas2026"));
        listaEstudiantes.add(new Estudiante("2021601981", "Urbiola Urquizo", "Hugo Raul", "Urbiola2026"));
        Collections.sort(listaEstudiantes, (a, b) -> a.apellidos.compareTo(b.apellidos));

        for (Estudiante e : listaEstudiantes) {
            baseDatosAsistencia.put(e.id, new ArrayList<>());
        }

        // Semillas de sesiones de prueba sugeridas
        // BUG-05: ya no nacen con tieneActividad = true por defecto
        String[] pruebas = {"04/05/2026", "05/05/2026", "06/05/2026"};
        for (String p : pruebas) {
            sesionesConfig.add(new ConfigFecha(p));
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
            // Asegurar contraseñas individuales en caso de compatibilidad (INC-01, INC-04)
            if (e.password == null || e.password.isEmpty()) {
                if (e.id.equals("2023203021")) e.password = "72807827";
                else if (e.id.equals("2023803011")) e.password = "Rojas2026";
                else if (e.id.equals("2021601981")) e.password = "Urbiola2026";
                else e.password = "72807827";
            }
            baseDatosAsistencia.putIfAbsent(e.id, new ArrayList<>());
        }

        // BUG-05: la verificación de estructura desactiva tieneActividad si no existe ningún registro
        for (ConfigFecha c : sesionesConfig) {
            c.fecha = limpiarFecha(c.fecha);
            if (!existeRegistroEnFecha(c.fecha)) {
                c.tieneActividad = false;
            }
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

    private boolean existeRegistroEnFecha(String f) {
        f = limpiarFecha(f);
        if (f == null || f.isEmpty()) return false;
        final String fechaBuscada = f;
        for (List<Registro> registros : baseDatosAsistencia.values()) {
            if (registros != null) {
                for (Registro r : registros) {
                    if (limpiarFecha(r.fecha).equals(fechaBuscada)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean credencialesEstudianteValidas(Estudiante st, String pass) {
        return st != null && pass.equals(st.password);
    }

    // --- LOGIN ---
    private void mostrarLogin() {
        setTitle("ACCESO - GRUPO 07 ASISTENCIA");
        setSize(400, 450);
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
                if (credencialesEstudianteValidas(st, pass)) {
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
        setSize(1200, 800); // Aumentado para evitar maximización incómoda
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
            @Override
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

        // Configurar estado inicial de controles
        String fechaInicial = obtenerFechaCombo(cbFechas.getSelectedItem());
        ConfigFecha cfInicial = buscarConfig(fechaInicial);
        if (cfInicial != null) {
            tglLock.setEnabled(true);
            chkEscritura.setEnabled(true);
            tglLock.setSelected(!cfInicial.bloqueado);
            actualizarVisualBloqueo(cfInicial, tglLock);
            chkEscritura.setSelected(cfInicial.habilitadoParaAlumno);
            refrescarTablaDocente(modReg, fechaInicial);
        } else {
            tglLock.setEnabled(false);
            tglLock.setText("Bloquear (Cerrar)");
            tglLock.setBackground(null);
            tglLock.setToolTipText(null);
            chkEscritura.setEnabled(false);
            chkEscritura.setSelected(false);
            refrescarTablaDocente(modReg, "");
        }

        // -- LÓGICA DOCENTE --
        btnAdd.addActionListener(e -> {
            // BUG-08: Uso correcto de sobrecarga de showInputDialog
            String valorDefecto = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String f = (String) JOptionPane.showInputDialog(
                    this,
                    "Fecha dd/mm/aaaa:",
                    "Agregar fecha",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    valorDefecto
            );
            
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
            cbFechas.setSelectedItem(etiquetaFecha(nc));
            JOptionPane.showMessageDialog(this, "Fecha agregada correctamente.");
        });

        btnDel.addActionListener(e -> {
            String f = obtenerFechaCombo(cbFechas.getSelectedItem());
            if (f.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No existe una sesión seleccionada para eliminar."); // BUG-01
                return;
            }
            int c = JOptionPane.showConfirmDialog(this, "¿Seguro quiere eliminar la sesión y todas sus asistencias relacionadas?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                sesionesConfig.removeIf(cf -> limpiarFecha(cf.fecha).equals(f));
                eliminarAsistenciasPorFecha(f);
                guardarDatos();
                actualizarComboFechas(cbFechas);
                
                // BUG-01: actualización de controles cuando ya no quedan sesiones
                String nuevoSel = obtenerFechaCombo(cbFechas.getSelectedItem());
                ConfigFecha cf = buscarConfig(nuevoSel);
                if (cf != null) {
                    tglLock.setEnabled(true);
                    chkEscritura.setEnabled(true);
                    tglLock.setSelected(!cf.bloqueado);
                    actualizarVisualBloqueo(cf, tglLock);
                    chkEscritura.setSelected(cf.habilitadoParaAlumno);
                    refrescarTablaDocente(modReg, nuevoSel);
                } else {
                    tglLock.setEnabled(false);
                    tglLock.setText("Bloquear (Cerrar)");
                    tglLock.setBackground(null);
                    tglLock.setToolTipText(null);
                    chkEscritura.setEnabled(false);
                    chkEscritura.setSelected(false);
                    refrescarTablaDocente(modReg, "");
                }
            }
        });

        tglLock.addActionListener(e -> {
            String f = obtenerFechaCombo(cbFechas.getSelectedItem());
            ConfigFecha conf = buscarConfig(f);
            if (conf != null) {
                conf.bloqueado = !tglLock.isSelected();
                
                // Si se bloquea, deshabilitar automáticamente el marcado para alumno
                if (conf.bloqueado) {
                    conf.habilitadoParaAlumno = false;
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
                
                actualizarVisualBloqueo(conf, tglLock); // UX-03
                actualizarComboFechas(cbFechas);
                cbFechas.setSelectedItem(etiquetaFecha(conf)); // Mantener selección con etiqueta actualizada (UX-01)
            }
        });

        chkEscritura.addActionListener(e -> {
            String f = obtenerFechaCombo(cbFechas.getSelectedItem());
            ConfigFecha conf = buscarConfig(f);
            if (conf != null) {
                // UX-04: Sin aviso al habilitar marcado en sesión bloqueada
                if (conf.bloqueado && chkEscritura.isSelected()) {
                    JOptionPane.showMessageDialog(this, "La sesión está bloqueada. Primero debe desbloquearla...");
                    chkEscritura.setSelected(false);
                    conf.habilitadoParaAlumno = false;
                } else {
                    conf.habilitadoParaAlumno = chkEscritura.isSelected();
                }
                guardarDatos();
                actualizarComboFechas(cbFechas);
                cbFechas.setSelectedItem(etiquetaFecha(conf));
            }
        });

        cbFechas.addActionListener(e -> {
            String f = obtenerFechaCombo(cbFechas.getSelectedItem());
            ConfigFecha cf = buscarConfig(f);
            if (cf != null) {
                tglLock.setEnabled(true);
                chkEscritura.setEnabled(true);
                tglLock.setSelected(!cf.bloqueado);
                actualizarVisualBloqueo(cf, tglLock); // UX-03
                chkEscritura.setSelected(cf.habilitadoParaAlumno);
                refrescarTablaDocente(modReg, f);
            }
        });

        ActionListener markL = al -> {
            String f = obtenerFechaCombo(cbFechas.getSelectedItem());
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
                // INC-02: Motivos libres con opción de "Otro"
                String justSel = solicitarMotivoAusenciaJustificada("Motivo de ausencia justificada");
                if (justSel == null || justSel.trim().isEmpty()) return;
                just = justSel;
            }

            // BUG-03: Evitar eliminación silenciosa al hacer doble clic en el mismo estado
            Registro actual = buscarReg(sid, f);
            if (actual != null && actual.estado.equals(est)) {
                int confirmar = JOptionPane.showConfirmDialog(
                        this,
                        "El estudiante ya tiene este estado. ¿Desea retirar el registro de asistencia?",
                        "Confirmar retiro de registro",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirmar == JOptionPane.YES_OPTION) {
                    obtenerRegistrosDeEstudiante(sid).remove(actual);
                } else {
                    return;
                }
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

    private void actualizarVisualBloqueo(ConfigFecha cf, JToggleButton tglLock) {
        if (cf == null) return;
        tglLock.setText(cf.bloqueado ? "Desbloquear" : "Bloquear (Cerrar)");
        tglLock.setToolTipText(cf.bloqueado ? "Sesión bloqueada" : "Sesión abierta");
        tglLock.setBackground(cf.bloqueado ? COLOR_BLOQUEADO : COLOR_DESBLOQUEADO);
        tglLock.setForeground(Color.WHITE);
        tglLock.setOpaque(true);
    }

    private String solicitarMotivoAusenciaJustificada(String titulo) {
        Object[] opciones = {"Salud", "Familia", "Trabajo", "Otro"};
        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un motivo o elija 'Otro' para escribir uno personalizado:",
                titulo,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        if (seleccion == null) return null;
        if (seleccion.equals("Otro")) {
            String personalizado = JOptionPane.showInputDialog(
                    this,
                    "Escriba el motivo:",
                    titulo,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (personalizado == null) return null;
            return limpiarTexto(personalizado);
        }
        return seleccion;
    }

    private String obtenerFechaCombo(Object item) {
        if (item == null) return "";
        String s = item.toString();
        int idx = s.indexOf('[');
        if (idx != -1) {
            s = s.substring(0, idx);
        }
        return limpiarFecha(s);
    }

    private String etiquetaFecha(ConfigFecha cf) {
        String estado = cf.bloqueado ? "Bloqueada" : "Abierta";
        String alumno = cf.habilitadoParaAlumno ? " - Alumno habilitado" : "";
        return limpiarFecha(cf.fecha) + " [" + estado + alumno + "]";
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
        for (Estudiante e : listaEstudiantes) {
            ResumenAsistencia resumen = calcularResumenEstudiante(e.id); // BUG-04: centralizado
            mod.addRow(new Object[]{
                    e.id,
                    e.apellidos + ", " + e.nombres,
                    resumen.presentes,
                    resumen.tardanzas,
                    resumen.ausenciasInjustificadas,
                    resumen.ausenciasJustificadas,
                    String.format("%.2f%%", resumen.porcentaje())
            });
        }
    }

    private ResumenAsistencia calcularResumenEstudiante(String sid) {
        ResumenAsistencia resumen = new ResumenAsistencia();
        for (ConfigFecha cf : sesionesConfig) {
            Registro r = buscarReg(sid, cf.fecha);
            
            if (esFechaFutura(cf.fecha)) {
                // Para fechas futuras, solo considerarlas si el alumno ya tiene registro (las marcó)
                if (r == null) {
                    continue;
                }
            } else {
                // Para fechas no-futuras, se consideran si el alumno marcó, o si la sesión está cerrada y tiene actividad
                if (r == null) {
                    if (!cf.bloqueado || !cf.tieneActividad) {
                        continue;
                    }
                }
            }
            
            resumen.sesiones++;
            String estado = (r == null) ? "AI" : limpiarTexto(r.estado);
            switch (estado) {
                case "P": resumen.presentes++; break;
                case "T": resumen.tardanzas++; break;
                case "AJ": resumen.ausenciasJustificadas++; break;
                default: resumen.ausenciasInjustificadas++; break;
            }
        }
        return resumen;
    }

    // --- VENTANA ESTUDIANTE ---
    private void mostrarVentanaEstudiante() {
        setTitle("Panel Alumno - Grupo 07 - " + currentUserName);
        setSize(1100, 750); // Aumentado para evitar maximización incómoda
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
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabH = new JTable(modH);
        tabH.setRowHeight(28);

        // Panel de estadísticas detalladas
        String[] headersStats = {"Indicador", "Cantidad"};
        DefaultTableModel modStatsAlumno = new DefaultTableModel(headersStats, 0) {
            @Override
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

            // BUG-06: Estudiante debe ingresar obligatoriamente motivo para AJ
            String cod = res.equals("Presente") ? "P" : res.equals("Tardanza") ? "T" : "AJ";
            String obs = "-";
            if (cod.equals("AJ")) {
                String motivo = JOptionPane.showInputDialog(this, "Ingrese el motivo de la ausencia justificada:");
                motivo = limpiarTexto(motivo);
                if (motivo == null || motivo.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Para registrar Ausencia Justificada debe ingresar un motivo.");
                    return;
                }
                obs = "por justificar: " + motivo;
            }

            int conf = JOptionPane.showConfirmDialog(this, "¿Desea guardar? Verifique antes de confirmar, no podrá editarlo luego.", "Guardar Asistencia", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                Registro r = new Registro(f, cod, obs);
                r.guardadoPorEstudiante = true;
                obtenerRegistrosDeEstudiante(currentUserId).removeIf(reg -> limpiarFecha(reg.fecha).equals(f));
                obtenerRegistrosDeEstudiante(currentUserId).add(r);
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
        ResumenAsistencia resumen = calcularResumenEstudiante(currentUserId); // BUG-04/BUG-07

        int totalRegistros = resumen.presentes + resumen.tardanzas + resumen.ausenciasInjustificadas + resumen.ausenciasJustificadas;

        lblTotal.setText("Registros: " + totalRegistros);
        lblP.setText("P: " + resumen.presentes);
        lblT.setText("T: " + resumen.tardanzas);
        lblAI.setText("AI: " + resumen.ausenciasInjustificadas);
        lblAJ.setText("AJ: " + resumen.ausenciasJustificadas);
        lblPorcentaje.setText(String.format("Asistencia: %.2f%%", resumen.porcentaje()));

        mod.setRowCount(0);
        mod.addRow(new Object[]{"Sesiones consideradas", resumen.sesiones});
        mod.addRow(new Object[]{"Presente", resumen.presentes});
        mod.addRow(new Object[]{"Tardanza", resumen.tardanzas});
        mod.addRow(new Object[]{"Ausencia injustificada", resumen.ausenciasInjustificadas});
        mod.addRow(new Object[]{"Ausencia justificada", resumen.ausenciasJustificadas});
        mod.addRow(new Object[]{"Porcentaje de asistencia", String.format("%.2f%%", resumen.porcentaje())});
    }

    private void refrescarHistorialAlumno(DefaultTableModel mod) {
        mod.setRowCount(0);
        List<ConfigFecha> reversa = new ArrayList<>(sesionesConfig);
        Collections.reverse(reversa);
        for (ConfigFecha f : reversa) {
            Registro r = buscarReg(currentUserId, f.fecha);
            
            // Omitir completamente sesiones futuras
        if (esFechaFutura(f.fecha)) {
           continue;
        }

            // Omitir sesiones abiertas sin registro del alumno actual
        if (r == null) {
            if (!f.bloqueado || !f.tieneActividad) {
                continue;
            }
    
        }

            String st = (r == null) ? "AI" : r.estado; // INC-05: Estado Pendiente no documentado, usar AI
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

    private boolean esFechaFutura(String f) {
        f = limpiarFecha(f);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date fechaSesion = sdf.parse(f);
            Date hoy = sdf.parse(sdf.format(new Date()));
            return fechaSesion.after(hoy);
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
        List<Registro> rs = obtenerRegistrosDeEstudiante(sid); // BUG-02
        if (rs == null || f == null || f.isEmpty()) return null;
        final String fechaBuscada = f;
        return rs.stream().filter(r -> limpiarFecha(r.fecha).equals(fechaBuscada)).findFirst().orElse(null);
    }

    private void actualizarRegManual(String sid, String f, String est, String j) {
        sid = limpiarTexto(sid);
        f = limpiarFecha(f);
        est = limpiarTexto(est);
        j = limpiarTexto(j);

        List<Registro> rs = obtenerRegistrosDeEstudiante(sid); // BUG-02
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
        for (ConfigFecha cf : sesionesConfig) {
            cb.addItem(etiquetaFecha(cf));
        }
    }

    private List<Registro> obtenerRegistrosDeEstudiante(String sid) {
        List<Registro> registros = baseDatosAsistencia.get(sid);
        if (registros == null) {
            registros = new ArrayList<>();
            baseDatosAsistencia.put(sid, registros);
        }
        return registros;
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
            for (Estudiante e : listaEstudiantes) {
                ResumenAsistencia resumen = calcularResumenEstudiante(e.id); // UX-05: centralizado
                fw.write(limpiarCSV(e.id) + "," + 
                         limpiarCSV(e.apellidos + " " + e.nombres) + "," + 
                         resumen.presentes + "," + 
                         resumen.tardanzas + "," + 
                         resumen.ausenciasInjustificadas + "," + 
                         resumen.ausenciasJustificadas + "," + 
                         limpiarCSV(String.format("%.2f%%", resumen.porcentaje())) + "\n");
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

                if (esFechaFutura(c.fecha)) {
                    continue;
                }

                if (r == null) {
                    if (!c.bloqueado || !c.tieneActividad) continue;
                }
                

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
