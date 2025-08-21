public class CaixaDeMarcha {
    private double[] marchas;
    private int marchaAtual;
    private Motor motor;
    private Roda roda;
    private boolean emMarchaRe;
    
    // Constantes para as marchas padrão e funcionamento
    private static final double[] MARCHAS_PADRAO = {3.5, 2.0, 1.3, 1.0, 0.8}; // 5 marchas
    private static final double RELACAO_MARCHA_RE = -3.0; // Relação negativa para marcha à ré
    private static final double EFICIENCIA_TRANSMISSAO = 0.95; // 95% de eficiência
    private static final int MARCHA_NEUTRA = -1; // Índice especial para neutro
    
    // Construtor principal - agora só recebe motor e roda
    public CaixaDeMarcha(Motor motor, Roda roda) {
        this.marchas = MARCHAS_PADRAO.clone(); // Usa marchas padrão
        this.motor = motor;
        this.roda = roda;
        this.marchaAtual = 0; // Primeira marcha (índice 0)
        this.emMarchaRe = false;
    }
    
    // Construtor alternativo para casos especiais (mantido para flexibilidade)
    public CaixaDeMarcha(Motor motor, Roda roda, double[] marchasPersonalizadas) {
        this.marchas = marchasPersonalizadas != null ? marchasPersonalizadas.clone() : MARCHAS_PADRAO.clone();
        this.motor = motor;
        this.roda = roda;
        this.marchaAtual = 0;
        this.emMarchaRe = false;
    }
    
    // Getters
    public double[] getMarchas() {
        return marchas.clone(); // Retorna cópia para manter encapsulamento
    }
    
    public int getMarchaAtual() {
        return marchaAtual;
    }
    
    public Motor getMotor() {
        return motor;
    }
    
    public Roda getRoda() {
        return roda;
    }
    
    public boolean isEmMarchaRe() {
        return emMarchaRe;
    }
    
    // Setters
    public void setMarchas(double[] marchas) {
        if (marchas != null && marchas.length > 0) {
            this.marchas = marchas.clone();
            // Valida marcha atual após mudança do array
            if (marchaAtual >= marchas.length) {
                marchaAtual = 0;
            }
        }
    }
    
    public void setMarchaAtual(int marchaAtual) {
        trocarMarcha(marchaAtual);
    }
    
    public void setMotor(Motor motor) {
        this.motor = motor;
    }
    
    public void setRoda(Roda roda) {
        this.roda = roda;
    }
    
    public void setEmMarchaRe(boolean emMarchaRe) {
        this.emMarchaRe = emMarchaRe;
        if (emMarchaRe) {
            this.marchaAtual = 0; // Usa primeira marcha como base para ré
        }
    }
    
    // Métodos principais da caixa de marcha
    public boolean trocarMarcha(int novaMarcha) {
        // Valida se a nova marcha está dentro do intervalo válido
        if (novaMarcha >= 0 && novaMarcha < marchas.length) {
            marchaAtual = novaMarcha;
            emMarchaRe = false; // Sai da marcha à ré ao trocar marcha normal
            return true;
        } else if (novaMarcha == MARCHA_NEUTRA) {
            marchaAtual = MARCHA_NEUTRA;
            emMarchaRe = false;
            return true;
        }
        return false; // Marcha inválida
    }
    
    public double getRelacaoAtual() {
        if (marchaAtual == MARCHA_NEUTRA) {
            return 0.0; // Neutro - sem transmissão
        }
        
        if (emMarchaRe) {
            return RELACAO_MARCHA_RE; // Relação negativa para marcha à ré
        }
        
        if (marchaAtual >= 0 && marchaAtual < marchas.length) {
            return marchas[marchaAtual];
        }
        
        return 0.0; // Fallback para situações inválidas
    }
    
    public double aplicarTransmissao() {
        if (motor == null || !motor.isLigado()) {
            return 0.0;
        }
        
        double relacaoAtual = getRelacaoAtual();
        if (relacaoAtual == 0.0) {
            return 0.0; // Neutro ou situação inválida
        }
        
        // Calcula torque transmitido: Torque_saída = Torque_entrada × Relação × Eficiência
        double torqueMotor = motor.getTorque();
        double torqueTransmitido = torqueMotor * Math.abs(relacaoAtual) * EFICIENCIA_TRANSMISSAO;
        
        // Mantém sinal negativo para marcha à ré
        if (relacaoAtual < 0) {
            torqueTransmitido = -torqueTransmitido;
        }
        
        return torqueTransmitido;
    }
    
    public void enviarTorqueParaRoda() {
        if (roda != null) {
            double torqueTransmitido = aplicarTransmissao();
            roda.receberForca(torqueTransmitido);
        }
    }
    
    public void atualizar() {
        // Executa atualização completa da caixa de marcha
        enviarTorqueParaRoda();
        
        // Atualiza RPM da roda baseado no RPM do motor
        atualizarRpmRoda();
    }
    
    // Métodos auxiliares privados
    private void atualizarRpmRoda() {
        if (motor != null && roda != null) {
            double relacaoAtual = getRelacaoAtual();
            
            if (relacaoAtual != 0.0) {
                // RPM da roda = RPM do motor / Relação da marcha
                double rpmMotor = motor.getRPM();
                double rpmRoda = rpmMotor / Math.abs(relacaoAtual);
                
                // Converte RPM da roda para velocidade angular
                roda.setVelocidadeAngular(rpmRoda);
            }
        }
    }
    
    // Métodos auxiliares públicos
    public boolean subirMarcha() {
        if (emMarchaRe) {
            return trocarMarcha(0); // Sai da ré para primeira marcha
        }
        
        if (marchaAtual == MARCHA_NEUTRA) {
            return trocarMarcha(0); // Sai do neutro para primeira marcha
        }
        
        return trocarMarcha(marchaAtual + 1);
    }
    
    public boolean descerMarcha() {
        if (emMarchaRe || marchaAtual <= 0) {
            return false; // Não pode descer da primeira marcha ou da ré
        }
        
        return trocarMarcha(marchaAtual - 1);
    }
    
    public boolean colocarNeutro() {
        return trocarMarcha(MARCHA_NEUTRA);
    }
    
    public boolean colocarMarchaRe() {
        if (motor != null && motor.getRPM() < 1000) { // Só permite ré em baixa rotação
            emMarchaRe = true;
            marchaAtual = 0; // Usa primeira marcha como base
            return true;
        }
        return false;
    }
    
    public int getNumeroMarchas() {
        return marchas.length; // Sempre 5 marchas padrão
    }
    
    public String getDescricaoMarchaAtual() {
        if (marchaAtual == MARCHA_NEUTRA) {
            return "N"; // Neutro
        } else if (emMarchaRe) {
            return "R"; // Ré
        } else {
            return String.valueOf(marchaAtual + 1); // Marcha 1, 2, 3, 4, 5
        }
    }
    
    public boolean estaEmNeutro() {
        return marchaAtual == MARCHA_NEUTRA;
    }
    
    public boolean podeSubirMarcha() {
        return !emMarchaRe && marchaAtual < marchas.length - 1 && marchaAtual != MARCHA_NEUTRA;
    }
    
    public boolean podeDescerMarcha() {
        return !emMarchaRe && marchaAtual > 0 && marchaAtual != MARCHA_NEUTRA;
    }
    
    public double getVelocidadeMaximaMarchaAtual() {
        if (motor == null || estaEmNeutro()) {
            return 0.0;
        }
        
        double relacaoAtual = Math.abs(getRelacaoAtual());
        if (relacaoAtual == 0.0) {
            return 0.0;
        }
        
        // Velocidade máxima baseada no RPM máximo do motor
        double rpmMaximoRoda = motor.getRpmMaximo() / relacaoAtual;
        
        if (roda != null) {
            // v = 2π × r × (RPM/60)
            return 2 * Math.PI * roda.getRaio() * (rpmMaximoRoda / 60.0);
        }
        
        return 0.0;
    }
    
    public double getMultiplicadorTorque() {
        return Math.abs(getRelacaoAtual()) * EFICIENCIA_TRANSMISSAO;
    }
    
    // Método para resetar para configuração padrão
    public void resetarParaPadrao() {
        this.marchas = MARCHAS_PADRAO.clone();
        this.marchaAtual = 0;
        this.emMarchaRe = false;
    }
    
    // Método para obter informações das marchas padrão
    public static double[] getMarchasPadrao() {
        return MARCHAS_PADRAO.clone();
    }
    
    @Override
    public String toString() {
        return String.format("Caixa de Marcha: %s (5 marchas + R), Relação: %.2f, Torque: %.2fNm → %.2fNm", 
                           getDescricaoMarchaAtual(), 
                           getRelacaoAtual(),
                           motor != null ? motor.getTorque() : 0.0,
                           aplicarTransmissao());
    }
}