public class Carro {
    // Atributos básicos do carro
    private String modelo;
    private String marca;
    private String cor;
    private double velocidadeAtual;
    
    // Componentes do carro
    private Tanque tanque;
    private Motor motor;
    private Roda roda;
    private CaixaDeMarcha caixaDeMarcha;
    private Painel painel;
    
    // Timer para atualizações automáticas
    private javax.swing.Timer timerAtualizacao;
    
    // Constantes para configuração padrão
    private static final double CAPACIDADE_TANQUE = 50.0;
    private static final double POTENCIA_MOTOR = 150.0;
    private static final double RPM_MAXIMO = 7000.0;
    private static final double RAIO_RODA = 0.3;
    private static final double MASSA_VEICULO = 1200.0;
    private static final String MODELO_PADRAO = "Sedan";
    private static final String MARCA_PADRAO = "AutoSim";
    private static final String COR_PADRAO = "Branco";
    
    // Construtor padrão
    public Carro() {
        this.modelo = MODELO_PADRAO;
        this.marca = MARCA_PADRAO;
        this.cor = COR_PADRAO;
        this.velocidadeAtual = 0.0;
        
        this.tanque = new Tanque(CAPACIDADE_TANQUE);
        this.motor = new Motor(POTENCIA_MOTOR, RPM_MAXIMO, tanque);
        this.roda = new Roda(RAIO_RODA, MASSA_VEICULO);
        this.caixaDeMarcha = new CaixaDeMarcha(motor, roda);
        this.painel = new Painel(this);
        
        iniciarTimerAtualizacao();
    }
    
    // Construtor alternativo com personalização
    public Carro(String marca, String modelo, String cor) {
        this.marca = marca;
        this.modelo = modelo;
        this.cor = cor;
        this.velocidadeAtual = 0.0;
        
        this.tanque = new Tanque(CAPACIDADE_TANQUE);
        this.motor = new Motor(POTENCIA_MOTOR, RPM_MAXIMO, tanque);
        this.roda = new Roda(RAIO_RODA, MASSA_VEICULO);
        this.caixaDeMarcha = new CaixaDeMarcha(motor, roda);
        this.painel = new Painel(this);
        
        iniciarTimerAtualizacao();
    }
    
    private void iniciarTimerAtualizacao() {
        timerAtualizacao = new javax.swing.Timer(50, e -> atualizar());
        timerAtualizacao.start();
    }
    
    // MODIFICADO: Método atualizar com integração das notificações
    public void atualizar() {
        if (motor.isLigado()) {
            // Atualiza motor (inclui consumo de combustível e verificação automática)
            motor.atualizar(0.05);
            
            // Verifica se motor ainda está ligado após atualização
            if (motor.isLigado()) {
                atualizarRpmMotor();
                caixaDeMarcha.atualizar();
                
                if (velocidadeAtual > 0) {
                    roda.aplicarResistenciaAr(0.3);
                    aplicarResistenciaNatural();
                }
            } else {
                // Motor desligou automaticamente - força desaceleração
                if (velocidadeAtual > 0) {
                    velocidadeAtual = Math.max(0, velocidadeAtual - 5.0);
                }
            }
        } else {
            // Motor desligado - aplica desaceleração natural
            if (velocidadeAtual > 0) {
                velocidadeAtual = Math.max(0, velocidadeAtual - 1.5);
            }
            motor.setRPM(0.0);
        }
    }
    
    // NOVO: Método para calcular RPM de forma correta
    public void atualizarRpmMotor() {
        if (!motor.isLigado()) {
            return;
        }
        
        double rpmBase = 800.0;
        double relacaoMarcha = Math.abs(caixaDeMarcha.getRelacaoAtual());
        double rpmPorVelocidade = velocidadeAtual * relacaoMarcha * 25.0;
        double rpmPorAcelerador = motor.getAcelerador() * 2000.0;
        
        double rpmTotal = rpmBase + rpmPorVelocidade + rpmPorAcelerador;
        rpmTotal = Math.min(rpmTotal, RPM_MAXIMO);
        
        motor.setRPM(rpmTotal);
    }
    
    private void aplicarResistenciaNatural() {
        if (motor.getAcelerador() == 0.0 && velocidadeAtual > 0) {
            double resistencia = 0.2;
            if (velocidadeAtual > 80) {
                resistencia += 0.15;
            }
            velocidadeAtual = Math.max(0, velocidadeAtual - resistencia);
        }
    }
    
    // Métodos principais de controle do carro
    public boolean ligar() {
        if (motor.isLigado()) {
            return true;
        }
        
        if (tanque.estaVazio()) {
            return false;
        }
        
        boolean sucesso = motor.ligar();
        if (sucesso) {
            caixaDeMarcha.trocarMarcha(0);
        }
        
        return sucesso;
    }
    
    public void desligar() {
        motor.setAcelerador(0.0);
        motor.desligar();
        caixaDeMarcha.colocarNeutro();
    }
    
    public void acelerar() {
        acelerar(0.02);
    }
    
    public void acelerar(double incremento) {
        if (!motor.isLigado() || caixaDeMarcha.estaEmNeutro()) {
            return;
        }
        
        double novoAcelerador = Math.min(1.0, motor.getAcelerador() + incremento);
        motor.setAcelerador(novoAcelerador);
        
        calcularVelocidadeComAceleracao();
    }
    
    private void calcularVelocidadeComAceleracao() {
        if (motor.getAcelerador() > 0 && !caixaDeMarcha.estaEmNeutro()) {
            double torqueMotor = motor.getTorque();
            double relacaoMarcha = Math.abs(caixaDeMarcha.getRelacaoAtual());
            double acelerador = motor.getAcelerador();
            
            double fatorConversao = 1.0 / relacaoMarcha;
            double incrementoBase = torqueMotor * fatorConversao * 0.015;
            double bonusAcelerador = acelerador * 0.4;
            
            double incrementoVelocidade = incrementoBase + bonusAcelerador;
            incrementoVelocidade = Math.min(incrementoVelocidade, 1.5);
            
            velocidadeAtual += incrementoVelocidade;
            
            double velocidadeMaximaMarcha = calcularVelocidadeMaximaMarcha();
            if (velocidadeAtual > velocidadeMaximaMarcha) {
                velocidadeAtual = velocidadeMaximaMarcha;
            }
            
            if (velocidadeAtual > 200.0) {
                velocidadeAtual = 200.0;
            }
        }
    }
    
    private double calcularVelocidadeMaximaMarcha() {
        double relacaoMarcha = Math.abs(caixaDeMarcha.getRelacaoAtual());
        
        if (relacaoMarcha > 3.0) return 60.0;
        else if (relacaoMarcha > 2.5) return 100.0;
        else if (relacaoMarcha > 1.5) return 140.0;
        else if (relacaoMarcha > 0.9) return 170.0;
        else return 200.0;
    }
    
    public void frear() {
        frear(0.15);
    }
    
    public void frear(double intensidade) {
        double novoAcelerador = Math.max(0.0, motor.getAcelerador() - intensidade);
        motor.setAcelerador(novoAcelerador);
        
        double reducaoVelocidade = intensidade * 8.0;
        velocidadeAtual = Math.max(0, velocidadeAtual - reducaoVelocidade);
        
        if (velocidadeAtual < 0.5) {
            velocidadeAtual = 0.0;
        }
    }
    
    public boolean subirMarcha() {
        if (!motor.isLigado()) {
            return false;
        }
        
        boolean sucesso = caixaDeMarcha.subirMarcha();
        return sucesso;
    }
    
    public boolean descerMarcha() {
        if (!motor.isLigado()) {
            return false;
        }
        
        boolean sucesso = caixaDeMarcha.descerMarcha();
        return sucesso;
    }
    
    public boolean colocarMarchaRe() {
        if (!motor.isLigado() || velocidadeAtual > 5.0) {
            return false;
        }
        
        return caixaDeMarcha.colocarMarchaRe();
    }
    
    public boolean colocarNeutro() {
        return caixaDeMarcha.colocarNeutro();
    }
    
    public boolean abastecer(double litros) {
        return tanque.abastecerCombustivel(litros);
    }
    
    // Getters para atributos básicos
    public String getModelo() { return modelo; }
    public String getMarca() { return marca; }
    public String getCor() { return cor; }
    public double getVelocidadeAtual() { return velocidadeAtual; }
    
    // Getters para componentes
    public Tanque getTanque() { return tanque; }
    public Motor getMotor() { return motor; }
    public Roda getRoda() { return roda; }
    public CaixaDeMarcha getCaixaDeMarcha() { return caixaDeMarcha; }
    public Painel getPainel() { return painel; }
    
    // Métodos de informação
    public boolean estaLigado() { return motor.isLigado(); }
    public boolean temCombustivel() { return !tanque.estaVazio(); }
    public double getNivelCombustivel() { return tanque.getNivelAtual(); }
    public double getCapacidadeTanque() { return tanque.getCapacidadeMaxima(); }
    public double getRpmAtual() { return motor.getRPM(); }
    public String getMarchaAtual() { return caixaDeMarcha.getDescricaoMarchaAtual(); }
    public double getTorqueAtual() { return motor.getTorque(); }
    public double getAceleradorAtual() { return motor.getAcelerador(); }
    public double getForcaTracao() { return roda.getForcaTracao(); }
    public double getPercentualCombustivel() { return tanque.getPercentualCombustivel(); }
    public double getPercentualRpm() { return motor.getPercentualRpm(); }
    public boolean estaAcelerado() { return motor.estaAcelerado(); }
    public boolean estaNoLimite() { return motor.estaNoLimite(); }
    public boolean podeSubirMarcha() { return caixaDeMarcha.podeSubirMarcha(); }
    public boolean podeDescerMarcha() { return caixaDeMarcha.podeDescerMarcha(); }
    
    public String getStatusGeral() {
        return String.format("%s %s %s - %s - %.1f km/h", 
                           marca, modelo, cor,
                           motor.isLigado() ? "LIGADO" : "DESLIGADO",
                           velocidadeAtual);
    }
    
    public void exibirPainel() {
        if (painel != null) {
            painel.exibirInformacoes();
        }
    }
    
    public void fecharPainel() {
        if (timerAtualizacao != null) {
            timerAtualizacao.stop();
        }
        if (painel != null) {
            painel.fechar();
        }
    }
    
    public void resetar() {
        desligar();
        motor.setAcelerador(0.0);
        caixaDeMarcha.colocarNeutro();
        velocidadeAtual = 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("Carro: %s %s %s\nStatus: %s\nVelocidade: %.1f km/h\nCombustível: %.1f/%.1fL\nMarcha: %s\nRPM: %.0f", 
                           marca, modelo, cor,
                           motor.isLigado() ? "LIGADO" : "DESLIGADO",
                           velocidadeAtual,
                           tanque.getNivelAtual(), tanque.getCapacidadeMaxima(),
                           caixaDeMarcha.getDescricaoMarchaAtual(),
                           motor.getRPM());
    }
    
    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            System.err.println("Erro ao configurar look and feel: " + e.getMessage());
        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Carro carro = new Carro("Toyota", "Corolla", "Prata");
                carro.abastecer(30.0);
                
                System.out.println("=== SIMULADOR DE CARRO INICIADO ===");
                System.out.println(carro.toString());
                System.out.println("Interface gráfica aberta!");
                System.out.println("=====================================");
            }
        });
    }
}
