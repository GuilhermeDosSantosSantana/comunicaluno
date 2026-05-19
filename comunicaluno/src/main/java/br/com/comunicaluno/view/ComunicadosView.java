package br.com.comunicaluno.view;

import br.com.comunicaluno.model.Comunicado;
import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.ComunicadoService;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComunicadosView extends JFrame {

    private Usuario usuarioLogado;
    private ComunicadoService comunicadoService;
    
    private JPanel painelFeed; // Onde os avisos vão aparecer

    public ComunicadosView(Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            throw new SecurityException("Acesso negado.");
        }
        this.usuarioLogado = usuarioLogado;
        this.comunicadoService = new ComunicadoService();
        
        configurarJanela();
        inicializarComponentes();
        atualizarMural();
    }

    private void configurarJanela() {
        setTitle("ComunicAluno - Mural de Avisos");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        // --- FEED DE AVISOS (Centro) ---
        painelFeed = new JPanel();
        painelFeed.setLayout(new BoxLayout(painelFeed, BoxLayout.Y_AXIS));
        painelFeed.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(painelFeed);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // --- PAINEL DE PUBLICAÇÃO (Apenas para Professores, Coordenadores e Admins) ---
        if (!"ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            JPanel painelPublicar = criarPainelPublicacao();
            add(painelPublicar, BorderLayout.SOUTH);
        }
    }

    private JPanel criarPainelPublicacao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Publicar Novo Aviso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Título:"), gbc);
        JTextField txtTitulo = new JTextField();
        gbc.gridx = 1; gbc.weightx = 1.0;
        painel.add(txtTitulo, gbc);

        // Público Alvo
        gbc.gridx = 2; gbc.weightx = 0.0;
        painel.add(new JLabel("Para:"), gbc);
        JComboBox<String> comboPublico = new JComboBox<>(new String[]{"TODOS", "ALUNOS", "PROFESSORES", "COORDENADORES"});
        gbc.gridx = 3;
        painel.add(comboPublico, gbc);

        // Mensagem
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Mensagem:"), gbc);
        JTextArea txtMensagem = new JTextArea(3, 20);
        txtMensagem.setLineWrap(true);
        JScrollPane scrollMensagem = new JScrollPane(txtMensagem);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        painel.add(scrollMensagem, gbc);

        // Botão Publicar
        JButton btnPublicar = new JButton("Publicar");
        btnPublicar.setBackground(new Color(33, 150, 243));
        btnPublicar.setForeground(Color.WHITE);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        painel.add(btnPublicar, gbc);

        // Ação do Botão
        btnPublicar.addActionListener(e -> {
            try {
                Comunicado novo = new Comunicado(
                        usuarioLogado.getIdUsuario(),
                        txtTitulo.getText(),
                        txtMensagem.getText(),
                        (String) comboPublico.getSelectedItem()
                );
                
                comunicadoService.publicarAviso(novo, usuarioLogado);
                JOptionPane.showMessageDialog(this, "Aviso publicado com sucesso!");
                
                txtTitulo.setText("");
                txtMensagem.setText("");
                atualizarMural(); // Recarrega o mural instantaneamente
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }

    private void atualizarMural() {
        painelFeed.removeAll(); // Limpa os avisos antigos da interface
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<Comunicado> avisos = comunicadoService.carregarMural(usuarioLogado);
        
        if (avisos.isEmpty()) {
            painelFeed.add(new JLabel("  Nenhum comunicado disponível no momento."));
        } else {
            for (Comunicado aviso : avisos) {
                JPanel card = new JPanel(new BorderLayout());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(10, 10, 5, 10),
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)
                ));
                card.setBackground(new Color(245, 245, 245));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

                // Cabeçalho do Card (Título + Autor + Data)
                String dataFormatada = aviso.getCreatedAt() != null ? aviso.getCreatedAt().format(formatter) : "Agora";
                JLabel lblCabecalho = new JLabel("<html><b>" + aviso.getTitulo() + "</b> <br><small>Por: " + aviso.getNomeAutor() + " (" + dataFormatada + ") | Para: " + aviso.getDestinatarioTipo() + "</small></html>");
                lblCabecalho.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                // Corpo do Card (Mensagem)
                JTextArea txtCorpo = new JTextArea(aviso.getMensagem());
                txtCorpo.setWrapStyleWord(true);
                txtCorpo.setLineWrap(true);
                txtCorpo.setOpaque(false);
                txtCorpo.setEditable(false);
                txtCorpo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                card.add(lblCabecalho, BorderLayout.NORTH);
                card.add(txtCorpo, BorderLayout.CENTER);
                
                painelFeed.add(card);
            }
        }
        
        painelFeed.revalidate(); // Avisa o Swing para redesenhar o ecrã
        painelFeed.repaint();
    }
}