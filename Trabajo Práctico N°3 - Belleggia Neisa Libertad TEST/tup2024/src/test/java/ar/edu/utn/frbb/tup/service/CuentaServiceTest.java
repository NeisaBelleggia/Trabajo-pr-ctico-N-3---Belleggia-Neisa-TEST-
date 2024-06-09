package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.TipoPersona;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaNotSupportedExcepcion;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class CuentaServiceTest {

    @Mock
    private CuentaDao cuentaDao;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private CuentaService cuentaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //MÉTODOS SOLICITADOS PARA CuentaServiceTest.java
    //1 - cuenta existente
    //2 - cuenta soportada y cuenta no soportada
    //3 - cliente ya tiene cuenta de ese tipo
    //4 - cuenta creada exitosamente y cuenta creada exitosamente con Cuenta Corriente

    @Test
    public void testCuentaExistente() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cuenta cuentaExistente = new Cuenta();
        cuentaExistente.setNumeroCuenta(12345);

        when(cuentaDao.find(12345)).thenReturn(cuentaExistente);

        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuentaExistente, 12345678));
    }

    @Test
    public void testTipoDeCuentaSoportada() {
        assertTrue(cuentaService.tipoDeCuentaSoportada("CA$"));
        assertTrue(cuentaService.tipoDeCuentaSoportada("CC$"));
        assertTrue(cuentaService.tipoDeCuentaSoportada("CAU$S"));
        assertFalse(cuentaService.tipoDeCuentaSoportada(null)); 
    }

    @Test
    public void testCuentaNoSoportada() {
        Cuenta cuentaNueva = new Cuenta()
                .setMoneda(TipoMoneda.DOLARES)
                .setBalance(200000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO); 
    
        assertThrows(CuentaNotSupportedExcepcion.class, () -> cuentaService.validarCuentaSoportada(cuentaNueva.getTipoCuenta()));
    }
    
    @Test
    public void testClienteYaTieneCuentaDeEseTipo() throws TipoCuentaAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setDni(12345678);
        Cuenta cuentaExistente = new Cuenta();
        cuentaExistente.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cliente.addCuenta(cuentaExistente);

        Cuenta cuentaNueva = new Cuenta();
        cuentaNueva.setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteService.buscarClientePorDni(12345678)).thenReturn(cliente);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuentaNueva, 12345678));

        verify(clienteService, times(1)).buscarClientePorDni(12345678);
    }

    @Test
    public void testCuentaCreadaExitosamente() throws Exception, ClienteAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cuenta cuentaNueva = new Cuenta();
        cuentaNueva.setNumeroCuenta(12345);

        when(cuentaDao.find(12345)).thenReturn(null);

        Cliente cliente = new Cliente();
        cliente.setDni(12345678);
        when(clienteService.buscarClientePorDni(12345678)).thenReturn(cliente);

        doNothing().when(clienteService).agregarCuenta(any(Cuenta.class), anyLong());

        assertDoesNotThrow(() -> cuentaService.darDeAltaCuenta(cuentaNueva, 12345678));

        verify(cuentaDao, times(1)).save(cuentaNueva);
    }

    @Test
    public void testCuentaCreadaExitosamenteConCuentaCorriente()
            throws Exception, ClienteAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cuenta cuentaNueva = new Cuenta();
        cuentaNueva.setNumeroCuenta(67890);
        cuentaNueva.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);

        when(cuentaDao.find(67890)).thenReturn(null);

        Cliente cliente = new Cliente();
        cliente.setDni(87654321);
        when(clienteService.buscarClientePorDni(87654321)).thenReturn(cliente);

        doNothing().when(clienteService).agregarCuenta(any(Cuenta.class), anyLong());

        assertDoesNotThrow(() -> cuentaService.darDeAltaCuenta(cuentaNueva, 87654321));

        verify(cuentaDao, times(1)).save(cuentaNueva);

        verify(clienteService, times(1)).agregarCuenta(cuentaNueva, 87654321);
    }
}