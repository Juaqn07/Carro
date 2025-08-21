public class Roda {
    private double raio;
    private double velocidadeAngular;
    private double torqueRecebido;
    private double velocidadeLinear;
    private double forcaTracao;
    private double massaDoVeiculo;
    private double coeficienteAtrito;
    
    // Constantes para cálculos físicos
    private static final double COEFICIENTE_ATRITO_PADRAO = 0.7; // Atrito médio pneu/asfalto
    private static final double INERCIA_RODA = 0.5; // Momento de inércia simplificado
    private static final double RESISTENCIA_ROLAMENTO = 0.015; // Resistência ao rolamento
    
    // Construtor principal
    public Roda(double raio, double massaDoVeiculo) {
        this.raio = raio;
        this.massaDoVeiculo = massaDoVeiculo;
        this.coeficienteAtrito = COEFICIENTE_ATRITO_PADRAO;
        this.velocidadeAngular = 0.0;
        this.torqueRecebido = 0.0;
        this.velocidadeLinear = 0.0;
        this.forcaTracao = 0.0;
    }
    
    // Getters
    public double getRaio() {
        return raio;
    }
    
    public double getVelocidadeAngular() {
        return velocidadeAngular;
    }
    
    public double getTorqueRecebido() {
        return torqueRecebido;
    }
    
    public double getVelocidadeLinear() {
        return velocidadeLinear;
    }
    
    public double getForcaTracao() {
        return forcaTracao;
    }
    
    public double getMassaDoVeiculo() {
        return massaDoVeiculo;
    }
    
    public double getCoeficienteAtrito() {
        return coeficienteAtrito;
    }
    
    // Setters
    public void setRaio(double raio) {
        if (raio > 0) {
            this.raio = raio;
            // Recalcula velocidade linear com o novo raio
            calcularVelocidadeLinear();
        }
    }
    
    public void setVelocidadeAngular(double velocidadeAngular) {
        if (velocidadeAngular >= 0) {
            this.velocidadeAngular = velocidadeAngular;
            calcularVelocidadeLinear();
        }
    }
    
    public void setTorqueRecebido(double torqueRecebido) {
        this.torqueRecebido = torqueRecebido;
    }
    
    public void setVelocidadeLinear(double velocidadeLinear) {
        if (velocidadeLinear >= 0) {
            this.velocidadeLinear = velocidadeLinear;
        }
    }
    
    public void setForcaTracao(double forcaTracao) {
        this.forcaTracao = forcaTracao;
    }
    
    public void setMassaDoVeiculo(double massaDoVeiculo) {
        if (massaDoVeiculo > 0) {
            this.massaDoVeiculo = massaDoVeiculo;
        }
    }
    
    public void setCoeficienteAtrito(double coeficienteAtrito) {
        if (coeficienteAtrito >= 0 && coeficienteAtrito <= 1.0) {
            this.coeficienteAtrito = coeficienteAtrito;
        }
    }
    
    // Método principal para aplicar torque
    public void aplicarTorque(double torque) {
        this.torqueRecebido = torque;
        
        // Calcula força de tração: F = T / r
        if (raio > 0) {
            this.forcaTracao = torque / raio;
        }
        
        // Limita a força de tração pelo atrito disponível
        double forcaMaximaAtrito = calcularForcaMaximaAtrito();
        if (this.forcaTracao > forcaMaximaAtrito) {
            this.forcaTracao = forcaMaximaAtrito;
        }
        
        // Atualiza velocidade angular baseada no torque aplicado
        atualizarVelocidadeAngular(torque);
        
        // Recalcula velocidade linear
        calcularVelocidadeLinear();
    }
    
    // Interface pública para receber torque da caixa de marcha
    public void receberForca(double torqueTransmitido) {
        aplicarTorque(torqueTransmitido);
    }
    
    // Método para entregar velocidade linear (interface para o painel)
    public double entregarVelocidade() {
        return velocidadeLinear;
    }
    
    // Métodos auxiliares privados
    private void calcularVelocidadeLinear() {
        // v = 2 * π * r * (RPM / 60)
        this.velocidadeLinear = 2 * Math.PI * raio * (velocidadeAngular / 60.0);
    }
    
    private void atualizarVelocidadeAngular(double torque) {
        // Simulação simplificada da aceleração angular
        // Considera resistências e inércia
        double torqueLiquido = torque - calcularTorqueResistencia();
        double aceleracaoAngular = torqueLiquido / (INERCIA_RODA * massaDoVeiculo);
        
        // Atualiza velocidade angular (simplificado para 1 segundo)
        this.velocidadeAngular += aceleracaoAngular * 9.55; // Conversão rad/s² para RPM/s
        
        // Garante que não seja negativa
        if (this.velocidadeAngular < 0) {
            this.velocidadeAngular = 0;
        }
    }
    
    private double calcularForcaMaximaAtrito() {
        // F_max = μ * m * g (simplificado)
        double peso = massaDoVeiculo * 9.81; // Aceleração da gravidade
        return coeficienteAtrito * peso;
    }
    
    private double calcularTorqueResistencia() {
        // Torque de resistência ao rolamento
        double forcaResistencia = RESISTENCIA_ROLAMENTO * massaDoVeiculo * 9.81;
        return forcaResistencia * raio;
    }
    
    // Métodos auxiliares públicos
    public double getVelocidadeKmh() {
        return velocidadeLinear * 3.6; // Conversão m/s para km/h
    }
    
    public double getCircunferencia() {
        return 2 * Math.PI * raio;
    }
    
    public boolean estaPatinando() {
        double forcaMaxima = calcularForcaMaximaAtrito();
        return forcaTracao > forcaMaxima * 0.95; // 95% do limite
    }
    
    public void frear(double intensidadeFreio) {
        if (intensidadeFreio >= 0 && intensidadeFreio <= 1.0) {
            // Reduz velocidade angular proporcionalmente à intensidade do freio
            double reducao = velocidadeAngular * intensidadeFreio * 0.1;
            velocidadeAngular = Math.max(0, velocidadeAngular - reducao);
            calcularVelocidadeLinear();
        }
    }
    
    public void aplicarResistenciaAr(double coeficienteAerodinamico) {
        // Resistência do ar aumenta com o quadrado da velocidade
        double resistenciaAr = coeficienteAerodinamico * velocidadeLinear * velocidadeLinear;
        double torqueResistenciaAr = resistenciaAr * raio;
        
        // Reduz velocidade angular devido à resistência
        double reducaoRPM = torqueResistenciaAr / (INERCIA_RODA * massaDoVeiculo) * 0.1;
        velocidadeAngular = Math.max(0, velocidadeAngular - reducaoRPM);
        calcularVelocidadeLinear();
    }
    
    @Override
    public String toString() {
        return String.format("Roda: %.2fm raio, %.1f RPM, %.2f m/s (%.1f km/h), Força: %.2fN", 
                           raio, velocidadeAngular, velocidadeLinear, getVelocidadeKmh(), forcaTracao);
    }
}