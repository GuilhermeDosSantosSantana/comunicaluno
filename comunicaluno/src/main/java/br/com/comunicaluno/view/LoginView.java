package br.com.comunicaluno.view;

import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.UsuarioService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginView extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JButton btnLogin;
    private JButton btnRegistar; // Para navegar para o ecrã de registo

    private UsuarioService usuarioService;

    public LoginView() {
        this.usuarioService = new UsuarioService();
        configurarJanela();
        inicializarComponentes();
    }

    private void configurarJanela() {
        setTitle("ComunicAluno - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza no ecrã
        setLayout(new GridBagLayout());
    }

    private void inicializarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label e Campo Email
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        add(txtEmail, gbc);

        // Label e Campo Senha
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Palavra-passe:"), gbc);
        gbc.gridx = 1;
        txtSenha = new JPasswordField(20);
        add(txtSenha, gbc);

        // Painel para os botões ficarem lado a lado
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        // Botão de Login
        btnLogin = new JButton("Entrar");
        btnLogin.setBackground(new Color(76, 175, 80)); // Verde
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(this::executarLogin);
        painelBotoes.add(btnLogin);

        // Botão para ir para o Registo
        btnRegistar = new JButton("Criar Conta");
        btnRegistar.setFocusPainted(false);
        btnRegistar.addActionListener(e -> {
            new CadastroUsuarioView().setVisible(true); // Abre o ecrã de registo
            this.dispose(); // Fecha o ecrã de login
        });
        painelBotoes.add(btnRegistar);

        // Adiciona o painel de botões à janela
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2; // Ocupa duas colunas
        add(painelBotoes, gbc);
    }

    private void executarLogin(ActionEvent e) {
        String email = txtEmail.getText();
        String senha = new String(txtSenha.getPassword());

        if (email.trim().isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha o e-mail e a palavra-passe.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Tenta autenticar através do serviço (Onde a lógica de negócio real reside)
            Usuario usuarioLogado = usuarioService.autenticar(email, senha);

            // Sucesso!
            JOptionPane.showMessageDialog(this, "Bem-vindo(a), " + usuarioLogado.getNome() + "!", "Login Bem-sucedido", JOptionPane.INFORMATION_MESSAGE);
            
            // Aqui, no futuro, abriremos o Dashboard Principal e passaremos o utilizador logado para ele.
             new DashboardView(usuarioLogado).setVisible(true);
            
            this.dispose(); // Fecha o ecrã de login

        } catch (SecurityException ex) {
            // Falha na autenticação (Credenciais erradas ou conta inativa)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            txtSenha.setText(""); // Limpa apenas a palavra-passe para facilitar nova tentativa
        } catch (Exception ex) {
            // Erro de ligação à base de dados ou outro problema técnico
            JOptionPane.showMessageDialog(this, "Erro interno ao tentar autenticar. Tente mais tarde.", "Erro Crítico", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}