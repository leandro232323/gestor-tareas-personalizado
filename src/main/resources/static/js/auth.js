
const formulario = document.getElementById("loginForm");

//modal general
const modalInformativo = document.getElementById("modal-general");
const modalInformativo_iconoError = document.getElementById("simbolo");
const tituloModalInformativo = document.getElementById("modalTitulo");
const mensajeModalInformativo = document.getElementById("modalMensaje");
const btnCerrar = document.getElementById("btnCerrarInfo");
const btnAceptar = document.getElementById("btnAceptarInfo");

function iniciar() {
    conexion();
    events();
}

function events() {
    formulario.addEventListener("submit", function (event) {
        event.preventDefault();
        const correo = document.getElementById("correo").value.trim();
        const password = document.getElementById("password").value.trim();

        if (correo == "" || password == "") {
            openModalInformativo("📪", "Campos vacios", "Faltan datos por llenar");
            return;
        }

        iniciarSesion(correo, password);
    });

    btnCerrar.addEventListener("click", function () {
        closeElement(modalInformativo);
    })

    btnAceptar.addEventListener("click", function () {
        closeElement(modalInformativo);
    })
}


async function iniciarSesion(correo, password) {
    try {

        const peticion = await fetch("/iniciar", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                correo_auth: correo,
                password_auth: password
            })
        });

        const datos = await peticion.json();
        if (datos.success == false) {
            openModalInformativo("❌", datos.titulo, datos.error);
            return;
        }

        let cedulaSeccion = datos.cedulaSeccion;        
        localStorage.setItem('cedulaUsuario', cedulaSeccion);

        // Redireccionar después de 1 segundo para que el usuario vea el mensaje
        window.location.href = "tareas.html";

    } catch (error) {
        openModalInformativo("❌", "Error fatal", error.message);
    }
}

async function conexion() {
    try {
        const peticion = await fetch("/test-conexion", {
            method: "GET",
            headers: { 'Content-Type': 'application/json' },
        })

        const datos = await peticion.json();
        if (datos.success == false) {
            openModalInformativo("❌", "Error Conexion base de datos", datos.error);
        }

        console.log(datos.message + " " + datos.database); // resultado de la conexion

    } catch (error) {
        openModalInformativo("❌", "Error fatal", error.message);
    }
}


function openModalInformativo(icono, titulo, mensaje) {
    modalInformativo_iconoError.textContent = icono;
    tituloModalInformativo.textContent = titulo;
    mensajeModalInformativo.textContent = mensaje;
    openElement(modalInformativo);
}

function openElement(elemento) {
    elemento.classList.remove("noMostrar");
    elemento.classList.add("mostrar");
}

function closeElement(elemento) {
    elemento.classList.remove("mostrar");
    elemento.classList.add("noMostrar");
}

window.addEventListener("DOMContentLoaded", iniciar)

