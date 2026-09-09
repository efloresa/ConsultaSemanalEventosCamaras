package atm.gob.ec.encriptacion;

public class Encriptador 
{
  public Encriptador()
  {
  }

  public static String encriptar(String ps_palabra)
  {
    return Twofish.encriptar(ps_palabra);
  }

  public static String decriptar(String ps_palabra)
  {
    return Twofish.decriptar(ps_palabra);
  }


}




