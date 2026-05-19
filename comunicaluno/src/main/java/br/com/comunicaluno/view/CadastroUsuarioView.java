package br.com.comunicaluno.view;

import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.UsuarioService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CadastroUsuarioView extends JFrame {

    private JTextField txtNome;
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JComboBox<String> comboPerfil;
    private JButton btnSalvar;
    
    private UsuarioService usuarioService;

    public CadastroUsuarioView() {
        this.usuarioService = new UsuarioService();
        configurarJanela();
        inicializarComponentes();
    }

    private void configurarJanela() {
        setTitle("ComunicAluno - Registo de Novo Utilizador");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza no ecrã
        setLayout(new GridBagLayout()); // Layout mais flexível e profissional
    }

    private void inicializarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label e Campo Nome
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nome Completo:"), gbc);
        gbc.gridx = 1;
        txtNome = new JTextField(20);
        add(txtNome, gbc);

        // Label e Campo Email
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        add(txtEmail, gbc);

        // Label e Campo Perfil
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Tipo de Perfil:"), gbc);
        gbc.gridx = 1;
        String[] perfis = {"ALUNO", "PROF", "COORDENADOR"};
        comboPerfil = new JComboBox<>(perfis);
        add(comboPerfil, gbc);

        // Label e Campo Senha
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Palavra-passe:"), gbc);
        gbc.gridx = 1;
        txtSenha = new JPasswordField(20);
        add(txtSenha, gbc);

        // Botão Salvar
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        btnSalvar = new JButton("Criar Conta");
        btnSalvar.setBackground(new Color(33, 150, 243));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.addActionListener(this::executarCadastro);
        add(btnSalvar, gbc);
    }

    private void executarCadastro(ActionEvent e) {
        try {
            // Coleta de dados (UI -> Model)
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String senha = new String(txtSenha.getPassword());
            String perfil = (String) comboPerfil.getSelectedItem();

            Usuario novoUsuario = new Usuario(nome, email, senha, perfil);

            // Delegação para o Serviço (A "mente" do sistema)
            usuarioService.registarNovoUsuario(novoUsuario);

            JOptionPane.showMessageDialog(this, 
                "Solicitação enviada! Aguarde a aprovação de um administrador.", 
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            limparCampos();

        } catch (IllegalArgumentException ex) {
            // Erro de validação de negócio (ex: email vazio)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            // Erro técnico inesperado (ex: banco fora do ar)
            JOptionPane.showMessageDialog(this, 
                "Ocorreu um erro interno. Tente novamente mais tarde.", 
                "Erro Crítico", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtEmail.setText("");
        txtSenha.setText("");
        comboPerfil.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        // Execução segura na Thread de UI do Swing
        SwingUtilities.invokeLater(() -> {
            new CadastroUsuarioView().setVisible(true);
        });
    }
}