import static spark.Spark.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Modelos.Usuario;

public class Servidor {
	
	/**
	 * Nuestra "Base de Datos" de Usuarios
	 * La utilizaremos para iniciar sesion
	 */
	static Usuario[] usuarios = {
	    new Usuario("Paul", "Santiago", "Paulsa", "paul"),
	    new Usuario("Albert", "Santiago", "albertsc", "albert"),
	    new Usuario("Emma", "Cruz", "emmasa", "emma"),
	    new Usuario("Pablo", "Santiago", "Pablosa", "pablo"),
	};
	
	public static void main(String[] args) {
		port(1234);
		staticFiles.location("/");
		
		Logger logger = LoggerFactory.getLogger(Servidor.class);
		
		before((req, res) -> {
		
			if(req.session(true).attribute("usuarioEnSesion") == null
				&& req.cookie("usuarioLoggeado") != null) {
				/**
				 * Dado que ambas condiciones se dan,
				 * podemos tomar el nombre de usuario de la cookie "usuarioLoggeado"
				 */
				String nombreDeUsuario = req.cookie("usuarioLoggeado");
				/**
				 * For Each.
				 * Se lee: Para cada `usuario` de la lista de `usuarios`
				 * se realiza el bloque de codigo a continuacion
				 */
				for(Usuario user : usuarios) {
					/**
					 * Si hay un usuario en la lista que tenga
					 * el mismo nombre de usuario de la variable
					 * `nombreDeUsuario`, entonces lo guardamos en la sesion
					 */
					if(user.getUsuario().equals(nombreDeUsuario)) {
						req.session(true).attribute("usuarioEnSesion", user);
					}
				}
			}
		});
		
		before("/privado/*", (req, res) -> {
			if(req.session(true).attribute("usuarioEnSesion") == null) {
				halt(401, "Usted no esta loggeado");
			}
		});
		
		get("/", (req, res) -> {
			if(req.session(true).attribute("usuarioEnSesion") == null) {
				res.redirect("/formulario.html");
				return null;
			}
			
			Usuario usuarioEnSesion = req.session(true).attribute("usuarioEnSesion");
			
			return "Bienvenido/a " + usuarioEnSesion.getNombre() + " " + usuarioEnSesion.getApellido();
		});
		
	
		post("/login", (req, res) -> {
			String nombreUsuario = req.queryParams("usuario");
			String contrasena = req.queryParams("pass");
			boolean rememberMe = req.queryParamOrDefault("rem", "off").equals("on");
			
			for(Usuario user : usuarios) {
				if(user.getUsuario().equals(nombreUsuario)
					&& user.getContrasena().equals(contrasena)) {
					req.session(true).attribute("usuarioEnSesion", user);
					if(rememberMe) {
						res.cookie("usuarioLoggeado", user.getUsuario());
					}
				}
			}

			res.redirect("/");
			return null;
		});
		
		
		
		get("/privado/este", (req, res) -> {
			return "Solo por usuarios loggeados ESTE";
		});
		
		get("/privado/tambien", (req, res) -> {
			return "Solo por usuarios loggeados TAMBIEN";
		});
		
		get("/home", (req, res) -> {
			res.status(302);
			return "302: Lo que solicitas ha sido movido";
		});
		
		
		get("/*", (req, res) -> {
			logger.info("Esta url no existe");
			res.status(404);
			return "404: Pagina no encontrada (Esta url no existe)";
		});
		
		

	}
}
