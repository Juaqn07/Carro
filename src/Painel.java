import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Painel extends JFrame {
    private Carro carro;
    
    // Componentes da interface
    private JLabel lblCombustivel;
    private JLabel lblVelocidade;
    private JLabel lblEstadoMotor;
    private JLabel lblRPM;
    private JLabel lblMarcha;
    
    // NOVO: Componentes para notificações
    private JPanel painelNotificacoes;
    private JLabel lblNotificacao;
    private Timer timerNotificacao;
    
    // Botões de controle
    private JButton btnAcelerar;
    private JButton btnFrear;
    private JButton btnSubirMarcha;
    private JButton btnDescerMarcha;
    private JButton btnLigarMotor;
    private JButton btnDesligarMotor;
    private JButton btnAbastecer;
    private JButton btnMarchaRe;
    private JButton btnNeutro;
    
    // Timers
    private Timer timerAtualizacao;
    private Timer timerAceleracao;
    private Timer timerFrenagem;
    
    public Painel(Carro carro) {
        this.carro = carro;
        
        setTitle("Simulador de Carro - Painel de Controle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        setResizable(false);
        
        inicializarComponentes();
        configurarLayout();
        adicionarListeners();
        
        timerAtualizacao = new Timer(100, e -> atualizarPainel());
        timerAtualizacao.start();
        
        // NOVO: Timer para verificar notificações
        timerNotificacao = new Timer(500, e -> verificarNotificacoes());
        timerNotificacao.start();
        
        atualizarPainel();
        setVisible(true);
    }
    
    private void inicializarComponentes() {
        // Labels de informação
        lblCombustivel = new JLabel("Combustível: 0.0 L");
        lblVelocidade = new JLabel("Velocidade: 0.0 km/h");
        lblEstadoMotor = new JLabel("Motor: DESLIGADO");
        lblRPM = new JLabel("RPM: 0");
        lblMarcha = new JLabel("Marcha: N");

        // NOVO: Componentes de notificação
        lblNotificacao = new JLabel("Sistema funcionando normalmente");
        lblNotificacao.setFont(new Font("Arial", Font.BOLD, 14));
        lblNotificacao.setHorizontalAlignment(SwingConstants.CENTER);
        lblNotificacao.setOpaque(true);
        lblNotificacao.setBackground(Color.WHITE);
        lblNotificacao.setForeground(Color.GRAY);
        lblNotificacao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        painelNotificacoes = new JPanel(new BorderLayout());
        painelNotificacoes.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Notificações do Sistema", 
            0, 0, new Font("Arial", Font.BOLD, 12)));
        painelNotificacoes.add(lblNotificacao, BorderLayout.CENTER);
        painelNotificacoes.setPreferredSize(new Dimension(880, 70));
        
        // Configuração visual dos labels
        Font fontInfo = new Font("Arial", Font.BOLD, 14);
        JLabel[] labels = {lblCombustivel, lblVelocidade, lblEstadoMotor, lblRPM, lblMarcha};
        
        for (JLabel label : labels) {
            label.setFont(fontInfo);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
        
        // Botões de controle
        btnAcelerar = new JButton("Acelerar");
        btnFrear = new JButton("Frear");
        btnSubirMarcha = new JButton("Subir Marcha");
        btnDescerMarcha = new JButton("Descer Marcha");
        btnLigarMotor = new JButton("Ligar Motor");
        btnDesligarMotor = new JButton("Desligar Motor");
        btnAbastecer = new JButton("Abastecer");
        btnMarchaRe = new JButton("Marcha Ré");
        btnNeutro = new JButton("Neutro");
        
        // Configuração visual dos botões
        Font fontBotao = new Font("Arial", Font.BOLD, 12);
        JButton[] botoes = {btnAcelerar, btnFrear, btnSubirMarcha, btnDescerMarcha, 
                           btnLigarMotor, btnDesligarMotor, btnAbastecer, btnMarchaRe, btnNeutro};
        
        for (JButton botao : botoes) {
            botao.setFont(fontBotao);
            botao.setPreferredSize(new Dimension(130, 45));
            botao.setFocusPainted(false);
        }
        
        // Cores específicas para alguns botões
        btnLigarMotor.setBackground(new Color(144, 238, 144));
        btnDesligarMotor.setBackground(new Color(255, 182, 193));
        btnAcelerar.setBackground(new Color(173, 216, 230));
        btnFrear.setBackground(new Color(255, 218, 185));
        btnAbastecer.setBackground(new Color(255, 255, 224));
        
        btnAcelerar.setToolTipText("Mantenha pressionado para acelerar continuamente");
        btnFrear.setToolTipText("Mantenha pressionado para frear continuamente");
    }
    
    // NOVO: Método para verificar e exibir notificações do motor
    private void verificarNotificacoes() {
        if (carro.getMotor().temNotificacaoNova()) {
            String mensagem = carro.getMotor().getUltimaNotificacao();
            String tipo = carro.getMotor().getTipoNotificacao();
            
            exibirNotificacao(mensagem, tipo);
        }
    }
    
    // NOVO: Método para exibir notificação no painel
    private void exibirNotificacao(String mensagem, String tipo) {
        lblNotificacao.setText(mensagem);
        
        // Define cor baseada no tipo
        switch (tipo) {
            case "CRITICO":
                lblNotificacao.setBackground(new Color(255, 200, 200));
                lblNotificacao.setForeground(new Color(139, 0, 0));
                break;
            case "AVISO":
                lblNotificacao.setBackground(new Color(255, 255, 200));
                lblNotificacao.setForeground(new Color(184, 134, 11));
                break;
            case "INFO":
                lblNotificacao.setBackground(new Color(200, 255, 200));
                lblNotificacao.setForeground(new Color(0, 100, 0));
                break;
            default:
                lblNotificacao.setBackground(Color.WHITE);
                lblNotificacao.setForeground(Color.BLACK);
        }
        
        // Pisca a notificação para chamar atenção
        if (tipo.equals("CRITICO")) {
            piscarNotificacao();
        }
        
        // Auto-limpa a notificação após 5 segundos
        Timer timerLimpeza = new Timer(5000, e -> {
            lblNotificacao.setText("Sistema funcionando normalmente");
            lblNotificacao.setBackground(Color.WHITE);
            lblNotificacao.setForeground(Color.GRAY);
            carro.getMotor().limparNotificacao();
        });
        timerLimpeza.setRepeats(false);
        timerLimpeza.start();
    }
    
    // NOVO: Método para piscar notificações críticas
    private void piscarNotificacao() {
        Timer timerPiscar = new Timer(300, null);
        final int[] contador = {0};
        
        timerPiscar.addActionListener(e -> {
            if (contador[0] < 6) {
                if (contador[0] % 2 == 0) {
                    lblNotificacao.setBackground(new Color(255, 100, 100));
                } else {
                    lblNotificacao.setBackground(new Color(255, 200, 200));
                }
                contador[0]++;
            } else {
                timerPiscar.stop();
                lblNotificacao.setBackground(new Color(255, 200, 200));
            }
        });
        
        timerPiscar.start();
    }
    
    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel superior - Informações do carro
        JPanel painelInfo = new JPanel();
        painelInfo.setLayout(new GridLayout(2, 4, 10, 10));
        painelInfo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Informações do Veículo", 
            0, 0, new Font("Arial", Font.BOLD, 16)));
        painelInfo.setBackground(new Color(240, 248, 255));
        
        painelInfo.add(lblEstadoMotor);
        painelInfo.add(lblVelocidade);
        painelInfo.add(lblCombustivel);
        painelInfo.add(lblRPM);
        painelInfo.add(lblMarcha);
        
        // NOVO: Painel superior completo (info + notificações)
        JPanel painelSuperior = new JPanel(new BorderLayout(5, 5));
        painelSuperior.add(painelInfo, BorderLayout.CENTER);
        painelSuperior.add(painelNotificacoes, BorderLayout.SOUTH);
        
        // Painel central - Controles do motor
        JPanel painelMotor = new JPanel();
        painelMotor.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelMotor.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Controle do Motor", 
            0, 0, new Font("Arial", Font.BOLD, 14)));
        painelMotor.setBackground(new Color(245, 245, 245));
        painelMotor.add(btnLigarMotor);
        painelMotor.add(btnDesligarMotor);
        painelMotor.add(btnAcelerar);
        painelMotor.add(btnFrear);
        
        // Painel de transmissão
        JPanel painelTransmissao = new JPanel();
        painelTransmissao.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelTransmissao.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Controle de Transmissão", 
            0, 0, new Font("Arial", Font.BOLD, 14)));
        painelTransmissao.setBackground(new Color(245, 245, 245));
        painelTransmissao.add(btnDescerMarcha);
        painelTransmissao.add(btnSubirMarcha);
        painelTransmissao.add(btnNeutro);
        painelTransmissao.add(btnMarchaRe);
        
        // Painel de combustível
        JPanel painelCombustivel = new JPanel();
        painelCombustivel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelCombustivel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Combustível", 
            0, 0, new Font("Arial", Font.BOLD, 14)));
        painelCombustivel.setBackground(new Color(245, 245, 245));
        painelCombustivel.add(btnAbastecer);
        
        // Painel de controles
        JPanel painelControles = new JPanel();
        painelControles.setLayout(new GridLayout(3, 1, 10, 10));
        painelControles.add(painelMotor);
        painelControles.add(painelTransmissao);
        painelControles.add(painelCombustivel);
        
        // Adiciona painéis ao frame principal
        add(painelSuperior, BorderLayout.NORTH);
        add(painelControles, BorderLayout.CENTER);
        
        // Painel de status na parte inferior
        JPanel painelStatus = new JPanel();
        painelStatus.setBorder(BorderFactory.createTitledBorder("Status do Sistema"));
        painelStatus.setBackground(new Color(248, 248, 255));
        JLabel lblStatus = new JLabel("Simulador de Carro - Sistema Ativo | Use os controles acima para operar o veículo");
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        lblStatus.setForeground(new Color(70, 70, 70));
        painelStatus.add(lblStatus);
        add(painelStatus, BorderLayout.SOUTH);
        
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void adicionarListeners() {
        // Listener para acelerar
        btnAcelerar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (timerAceleracao == null) {
                    timerAceleracao = new Timer(120, event -> {
                        carro.acelerar();
                        atualizarPainel();
                    });
                }
                timerAceleracao.start();
                btnAcelerar.setBackground(new Color(100, 149, 237));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (timerAceleracao != null) {
                    timerAceleracao.stop();
                }
                btnAcelerar.setBackground(new Color(173, 216, 230));
            }
        });
        
        // Listener para frear
        btnFrear.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (timerFrenagem == null) {
                    timerFrenagem = new Timer(100, event -> {
                        carro.frear();
                        atualizarPainel();
                    });
                }
                timerFrenagem.start();
                btnFrear.setBackground(new Color(255, 140, 0));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (timerFrenagem != null) {
                    timerFrenagem.stop();
                }
                btnFrear.setBackground(new Color(255, 218, 185));
            }
        });
        
        // MODIFICADO: Listeners sem JOptionPane
        btnSubirMarcha.addActionListener(e -> {
            boolean sucesso = carro.subirMarcha();
            if (!sucesso) {
                exibirNotificacao("Não é possível subir marcha no momento", "AVISO");
            }
            atualizarPainel();
        });
        
        btnDescerMarcha.addActionListener(e -> {
            boolean sucesso = carro.descerMarcha();
            if (!sucesso) {
                exibirNotificacao("Não é possível descer marcha no momento", "AVISO");
            }
            atualizarPainel();
        });
        
        btnLigarMotor.addActionListener(e -> {
            carro.ligar();
            atualizarPainel();
        });
        
        btnDesligarMotor.addActionListener(e -> {
            carro.getMotor().desligarComNotificacao();
            atualizarPainel();
        });
        
        btnAbastecer.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Digite a quantidade de combustível (litros):\n\n" +
                "Nível atual: " + String.format("%.1f", carro.getNivelCombustivel()) + "L\n" +
                "Capacidade: " + String.format("%.1f", carro.getCapacidadeTanque()) + "L\n" +
                "Espaço disponível: " + String.format("%.1f", carro.getCapacidadeTanque() - carro.getNivelCombustivel()) + "L\n", 
                "Abastecer", JOptionPane.QUESTION_MESSAGE);
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    double litros = Double.parseDouble(input);
                    if (litros > 0) {
                        boolean sucesso = carro.abastecer(litros);
                        if (sucesso) {
                            exibirNotificacao("Abastecimento realizado: " + String.format("%.1f", litros) + "L", "INFO");
                        } else {
                            exibirNotificacao("Tanque cheio! Abastecimento parcial realizado", "AVISO");
                        }
                    } else {
                        exibirNotificacao("Quantidade deve ser maior que zero!", "AVISO");
                    }
                } catch (NumberFormatException ex) {
                    exibirNotificacao("Valor inválido! Digite um número válido", "AVISO");
                }
            }
            atualizarPainel();
        });
        
        btnMarchaRe.addActionListener(e -> {
            boolean sucesso = carro.colocarMarchaRe();
            if (!sucesso) {
                exibirNotificacao("Não é possível engatar marcha ré - reduza a velocidade", "AVISO");
            } else {
                exibirNotificacao("Marcha ré engatada", "INFO");
            }
            atualizarPainel();
        });
        
        btnNeutro.addActionListener(e -> {
            carro.colocarNeutro();
            exibirNotificacao("Marcha neutra engatada", "INFO");
            atualizarPainel();
        });
    }
    
    // CORRIGIDO: Método atualizarPainel() com cores dinâmicas
    public void atualizarPainel() {
        // Atualiza informações do combustível
        double combustivel = carro.getTanque().getNivelAtual();
        double capacidade = carro.getTanque().getCapacidadeMaxima();
        double percentual = carro.getTanque().getPercentualCombustivel();
        
        // NOVO: Cor do combustível baseada no nível
        lblCombustivel.setText(String.format("Combustível: %.1f / %.1f L (%.1f%%)", combustivel, capacidade, percentual));
        if (percentual <= 5.0) {
            lblCombustivel.setForeground(Color.RED);
        } else if (percentual <= 15.0) {
            lblCombustivel.setForeground(new Color(255, 140, 0)); // Laranja
        } else {
            lblCombustivel.setForeground(Color.BLACK);
        }
        
        // Atualiza velocidade
        double velocidade = carro.getVelocidadeAtual();
        lblVelocidade.setText(String.format("Velocidade: %.1f km/h", velocidade));
        
        // Atualiza estado do motor
        boolean motorLigado = carro.estaLigado();
        lblEstadoMotor.setText("Motor: " + (motorLigado ? "LIGADO" : "DESLIGADO"));
        lblEstadoMotor.setForeground(motorLigado ? Color.GREEN : Color.RED);
        
        // Atualiza RPM com cor baseada na zona
        double rpm = carro.getMotor().getRPM();
        lblRPM.setText(String.format("RPM: %.0f", rpm));
        if (carro.getMotor().estaEmZonaVermelha()) {
            lblRPM.setForeground(Color.RED);
        } else if (rpm > carro.getMotor().getRpmMaximo() * 0.7) {
            lblRPM.setForeground(new Color(255, 140, 0)); // Laranja
        } else {
            lblRPM.setForeground(Color.BLACK);
        }
        
        // Atualiza marcha
        String marcha = carro.getCaixaDeMarcha().getDescricaoMarchaAtual();
        lblMarcha.setText("Marcha: " + marcha);
        
        // Habilita/desabilita botões baseado no estado
        boolean podeOperar = motorLigado;
        btnAcelerar.setEnabled(podeOperar && carro.temCombustivel());
        btnFrear.setEnabled(podeOperar || velocidade > 0); // Pode frear mesmo com motor desligado se houver velocidade
        btnSubirMarcha.setEnabled(podeOperar);
        btnDescerMarcha.setEnabled(podeOperar);
        btnMarchaRe.setEnabled(podeOperar);
        btnNeutro.setEnabled(podeOperar);
        
        btnLigarMotor.setEnabled(!motorLigado && carro.temCombustivel());
        btnDesligarMotor.setEnabled(motorLigado);
        
        // NOVO: Muda cor dos botões baseado no estado do combustível
        if (!carro.temCombustivel()) {
            btnAcelerar.setBackground(Color.LIGHT_GRAY);
            btnLigarMotor.setBackground(Color.LIGHT_GRAY);
        } else {
            if (btnAcelerar.isEnabled()) {
                btnAcelerar.setBackground(new Color(173, 216, 230));
            }
            if (btnLigarMotor.isEnabled()) {
                btnLigarMotor.setBackground(new Color(144, 238, 144));
            }
        }
    }
    
    // NOVO: Método para exibir notificação manual (para uso interno do painel)
    public void exibirNotificacaoManual(String mensagem, String tipo) {
        exibirNotificacao(mensagem, tipo);
    }
    
    // NOVO: Método para obter status detalhado do veículo
    public String getStatusDetalhado() {
        StringBuilder status = new StringBuilder();
        status.append("=== STATUS DETALHADO DO VEÍCULO ===\n");
        status.append(String.format("Motor: %s\n", carro.estaLigado() ? "LIGADO" : "DESLIGADO"));
        status.append(String.format("Velocidade: %.1f km/h\n", carro.getVelocidadeAtual()));
        status.append(String.format("RPM: %.0f\n", carro.getMotor().getRPM()));
        status.append(String.format("Marcha: %s\n", carro.getCaixaDeMarcha().getDescricaoMarchaAtual()));
        status.append(String.format("Combustível: %.1f/%.1fL (%.1f%%)\n", 
            carro.getNivelCombustivel(), carro.getCapacidadeTanque(), 
            carro.getTanque().getPercentualCombustivel()));
        status.append(String.format("Status Combustível: %s\n", carro.getMotor().getStatusCombustivel()));
        
        if (carro.getMotor().temNotificacaoNova()) {
            status.append(String.format("Última Notificação: %s\n", carro.getMotor().getUltimaNotificacao()));
        }
        
        return status.toString();
    }
    
    // Método para exibir informações detalhadas
    public void exibirInformacoes() {
        atualizarPainel();
        
        if (!isVisible()) {
            setVisible(true);
        }
        
        toFront();
        requestFocus();
    }
    
    // Método para pausar/retomar atualizações automáticas
    public void pausarAtualizacoes() {
        if (timerAtualizacao != null && timerAtualizacao.isRunning()) {
            timerAtualizacao.stop();
        }
        if (timerNotificacao != null && timerNotificacao.isRunning()) {
            timerNotificacao.stop();
        }
    }
    
    public void retomarAtualizacoes() {
        if (timerAtualizacao != null && !timerAtualizacao.isRunning()) {
            timerAtualizacao.start();
        }
        if (timerNotificacao != null && !timerNotificacao.isRunning()) {
            timerNotificacao.start();
        }
    }
    
    // Método para definir intervalo de atualização
    public void setIntervaloAtualizacao(int milissegundos) {
        if (timerAtualizacao != null) {
            timerAtualizacao.setDelay(milissegundos);
        }
    }
    
    // Getter para o carro
    public Carro getCarro() {
        return carro;
    }
    
    // MODIFICADO: Método para fechar o painel e parar todos os timers
    public void fechar() {
        if (timerAtualizacao != null) {
            timerAtualizacao.stop();
        }
        if (timerAceleracao != null) {
            timerAceleracao.stop();
        }
        if (timerFrenagem != null) {
            timerFrenagem.stop();
        }
        if (timerNotificacao != null) {
            timerNotificacao.stop();
        }
        dispose();
    }
    
    // Método main para teste
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            Carro carro = new Carro();
            new Painel(carro);
        });
    }
}
