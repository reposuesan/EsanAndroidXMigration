package pe.edu.esan.appostgrado.entidades

import pe.edu.esan.appostgrado.util.Utilitarios

/**
 * Created by lventura on 27/04/18.
 */
class PagoPre (val tipo: Utilitarios.TipoFila, val matricula: String, val codigo: String? = "", val concepto: String = "", val monto: String = "", val vencimiento: String = "")