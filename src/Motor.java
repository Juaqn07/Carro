public class Motor {
    private double rpm;
    private double torque;
    private double consumoCombustivel;
    private boolean ligado;
    private Tanque tanque;
    private double potenciaMaxima;
    private double rpmMaximo;
    private double acelerador;
    private double resistenciaMotor;
    
    // NOVO: Sistema de notificações para o painel
    private String ultimaNotificacao;
    private String tipoNotificacao; // "INFO", "AVISO", "CRITICO"
    private long tempoNotificacao;
    
    // Constantes para cálculos do motor
    private static final double TORQUE_MAXIMO_BASE = 300.0;
    private static final double RPM_MINIMO_FUNCIONAMENTO = 800.0;
    private static final double CONSUMO_BASE_POR_RPM = 0.000015;
    private static final double FATOR_CONSUMO_TORQUE = 0.000008;
    private static final double INERCIA_MOTOR = 0.2;
    private static final double RESISTENCIA_PADRAO = 15.0;
    
    // Construtor principal
    public Motor(double potenciaMaxima, double rpmMaximo, Tanque tanque) {
        this.potenciaMaxima = potenciaMaxima;
        this.rpmMaximo = rpmMaximo;
        this.tanque = tanque;
        this.rpm = 0.0;
        this.torque = 0.0;
        this.acelerador = 0.0;
        this.ligado = false;
        this.consumoCombustivel = CONSUMO_BASE_POR_RPM;
        this.resistenciaMotor = RESISTENCIA_PADRAO;
        
        // NOVO: Inicializa sistema de notificações
        this.ultimaNotificacao = "";
        this.tipoNotificacao = "INFO";
        this.tempoNotificacao = 0;
    }
    
    // NOVO: Métodos para sistema de notificações
    private void adicionarNotificacao(String mensagem, String tipo) {
        this.ultimaNotificacao = mensagem;
        this.tipoNotificacao = tipo;
        this.tempoNotificacao = System.currentTimeMillis();
    }
    
    public String getUltimaNotificacao() {
        return ultimaNotificacao;
    }
    
    public String getTipoNotificacao() {
        return tipoNotificacao;
    }
    
    public boolean temNotificacaoNova() {
        return !ultimaNotificacao.isEmpty() && 
               (System.currentTimeMillis() - tempoNotificacao) < 5000;
    }
    
    public void limparNotificacao() {
        this.ultimaNotificacao = "";
    }
    
    // CORRIGIDO: Método atualizar com notificações para o painel
    public void atualizar(double deltaTempo) {
        if (tanque == null || tanque.estaVazio()) {
            if (ligado) {
                adicionarNotificacao("Motor desligado - combustível esgotado!", "CRITICO");
            }
            desligar();
            return;
        }
        
        if (!ligado) {
            rpm = 0.0;
            torque = 0.0;
            return;
        }
        
        torque = gerarTorque();
        boolean conseguiuConsumir = consumirCombustivel(deltaTempo);
        
        if (!conseguiuConsumir || tanque.estaVazio()) {
            adicionarNotificacao("Motor parado - combustível insuficiente!", "CRITICO");
            desligar();
            return;
        }
        
        verificarNiveisCombustivel();
    }
    
    // NOVO: Método para verificar níveis de combustível e gerar avisos
    private void verificarNiveisCombustivel() {
        if (tanque == null) return;
        
        double percentual = tanque.getPercentualCombustivel();
        
        if (percentual <= 5.0 && percentual > 0) {
            adicionarNotificacao("ATENÇÃO: Combustível crítico (" + String.format("%.1f", percentual) + "%)", "CRITICO");
        } else if (percentual <= 15.0) {
            adicionarNotificacao("AVISO: Combustível na reserva (" + String.format("%.1f", percentual) + "%)", "AVISO");
        }
    }
    
    public boolean consumirCombustivel(double deltaTempo) {
        if (!ligado || tanque == null) {
            return false;
        }
        
        if (tanque.estaVazio()) {
            desligar();
            return false;
        }
        
        double consumoRpm = rpm * consumoCombustivel * deltaTempo;
        double consumoTorque = torque * FATOR_CONSUMO_TORQUE * deltaTempo;
        double consumoTotal = consumoRpm + consumoTorque;
        
        boolean sucesso = tanque.consumirCombustivel(consumoTotal);
        
        if (!sucesso || tanque.estaVazio()) {
            adicionarNotificacao("Combustível esgotado - motor desligando...", "CRITICO");
            desligar();
            return false;
        }
        
        return true;
    }
    
    public boolean ligar() {
        if (tanque == null) {
            adicionarNotificacao("Erro: Tanque não conectado!", "CRITICO");
            return false;
        }
        
        if (tanque.estaVazio()) {
            adicionarNotificacao("Não é possível ligar - tanque vazio!", "CRITICO");
            return false;
        }
        
        if (tanque.getNivelAtual() < 0.1) {
            adicionarNotificacao("Combustível insuficiente para ligar!", "CRITICO");
            return false;
        }
        
        ligado = true;
        rpm = RPM_MINIMO_FUNCIONAMENTO;
        torque = calcularTorqueMinimo();
        adicionarNotificacao("Motor ligado com sucesso!", "INFO");
        return true;
    }
    
    public void setAcelerador(double valor) {
        if (valor >= 0.0 && valor <= 1.0) {
            if (valor > 0 && (tanque == null || tanque.estaVazio())) {
                adicionarNotificacao("Não é possível acelerar - sem combustível!", "AVISO");
                this.acelerador = 0.0;
                desligar();
                return;
            }
            this.acelerador = valor;
        }
    }
    
    // NOVO: Método setRPM para controle direto do RPM
    public void setRPM(double rpm) {
        if (rpm >= 0 && rpm <= rpmMaximo) {
            this.rpm = rpm;
            if (ligado) {
                this.torque = gerarTorque();
            }
            
            if (rpm > rpmMaximo * 0.9 && ligado) {
                adicionarNotificacao("ATENÇÃO: Motor em zona vermelha!", "AVISO");
            }
        }
    }
    
    public void desligar() {
        ligado = false;
        acelerador = 0.0;
        rpm = 0.0;
        torque = 0.0;
    }
    
    // NOVO: Método desligar com notificação
    public void desligarComNotificacao() {
        desligar();
        adicionarNotificacao("Motor desligado", "INFO");
    }
    
    public double gerarTorque() {
        if (!ligado || rpm <= 0) {
            return 0.0;
        }
        
        double torqueMaximo = calcularTorqueMaximo();
        double fatorRpm = calcularFatorTorqueRpm();
        double torqueBruto = torqueMaximo * acelerador * fatorRpm;
        
        if (acelerador == 0.0 && rpm <= RPM_MINIMO_FUNCIONAMENTO * 1.2) {
            torqueBruto = calcularTorqueMinimo();
        }
        
        double torqueLiquido = aplicarResistenciaMotor(torqueBruto);
        return Math.max(0, torqueLiquido);
    }
    
    private double aplicarResistenciaMotor(double torqueBruto) {
        double resistenciaBase = resistenciaMotor;
        double resistenciaRpm = calcularResistenciaRpm();
        double resistenciaTotal = resistenciaBase + resistenciaRpm;
        return torqueBruto - resistenciaTotal;
    }
    
    private double calcularResistenciaRpm() {
        double fatorRpm = rpm / rpmMaximo;
        return resistenciaMotor * 0.3 * fatorRpm * fatorRpm;
    }
    
    private double calcularTorqueMaximo() {
        if (rpm > 100) {
            double omega = 2 * Math.PI * rpm / 60.0;
            double torquePorPotencia = (potenciaMaxima * 1000) / omega;
            return Math.min(torquePorPotencia, TORQUE_MAXIMO_BASE);
        }
        return TORQUE_MAXIMO_BASE;
    }
    
    private double calcularFatorTorqueRpm() {
        double rpmNormalizado = rpm / rpmMaximo;
        
        if (rpmNormalizado < 0.2) {
            return 0.6 + (rpmNormalizado / 0.2) * 0.4;
        } else if (rpmNormalizado < 0.6) {
            return 1.0;
        } else {
            return 1.0 - (rpmNormalizado - 0.6) * 0.4;
        }
    }
    
    private double calcularTorqueMinimo() {
        return TORQUE_MAXIMO_BASE * 0.08;
    }
    
    // Getters
    public double getRpm() { return rpm; }
    public double getTorque() { return torque; }
    public double getConsumoCombustivel() { return consumoCombustivel; }
    public boolean isLigado() { return ligado; }
    public Tanque getTanque() { return tanque; }
    public double getPotenciaMaxima() { return potenciaMaxima; }
    public double getRpmMaximo() { return rpmMaximo; }
    public double getAcelerador() { return acelerador; }
    public double getResistenciaMotor() { return resistenciaMotor; }
    public double getRPM() { return rpm; }
    
    public double getPotenciaAtual() {
        if (rpm > 0) {
            double omega = 2 * Math.PI * rpm / 60.0;
            return torque * omega / 1000.0;
        }
        return 0.0;
    }
    
    public double getPercentualRpm() {
        return (rpm / rpmMaximo) * 100.0;
    }
    
    public boolean estaAcelerado() {
        return acelerador > 0.1;
    }
    
    public boolean estaNoLimite() {
        return rpm >= rpmMaximo * 0.95;
    }
    
    public double getConsumoInstantaneo() {
        if (!ligado) return 0.0;
        return (rpm * consumoCombustivel) + (torque * FATOR_CONSUMO_TORQUE);
    }
    
    public boolean temCombustivel() {
        return tanque != null && !tanque.estaVazio();
    }
    
    public void acelerar(double incremento) {
        setAcelerador(Math.min(1.0, acelerador + incremento));
    }
    
    public void desacelerar(double decremento) {
        setAcelerador(Math.max(0.0, acelerador - decremento));
    }
    
    public boolean combustivelCritico() {
        if (tanque == null) return true;
        return tanque.getNivelAtual() < 2.0;
    }
    
    public boolean estaEmReserva() {
        if (tanque == null) return true;
        return tanque.getPercentualCombustivel() < 15.0;
    }
    
    public String getStatusCombustivel() {
        if (tanque == null) return "TANQUE DESCONECTADO";
        if (tanque.estaVazio()) return "VAZIO";
        if (combustivelCritico()) return "CRÍTICO";
        if (estaEmReserva()) return "RESERVA";
        return "NORMAL";
    }
    
    public boolean estaEmZonaVermelha() {
        return rpm > rpmMaximo * 0.85;
    }
    
    public double getTemperaturaSimulada() {
        if (!ligado) return 20.0;
        
        double tempBase = 80.0;
        double tempRpm = (rpm / rpmMaximo) * 40.0;
        double tempCarga = acelerador * 20.0;
        
        return tempBase + tempRpm + tempCarga;
    }
    
    @Override
    public String toString() {
        return String.format("Motor: %s, %.0f RPM (%.1f%%), %.2f Nm, Acelerador: %.1f%%, Temp: %.1f°C", 
                           ligado ? "LIGADO" : "DESLIGADO", 
                           rpm, getPercentualRpm(), 
                           torque, acelerador * 100, getTemperaturaSimulada());
    }
}
