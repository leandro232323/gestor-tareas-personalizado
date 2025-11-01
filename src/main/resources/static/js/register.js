const formulario = document.getElementById("registroForm");

const modalInformativo = document.getElementById("modal-general");
const modalInformativo_iconoError = document.getElementById("simbolo");
const tituloModalInformativo = document.getElementById("modalTitulo");
const mensajeModalInformativo = document.getElementById("modalMensaje");
const btnCerrar = document.getElementById("btnCerrarInfo");
const btnAceptar = document.getElementById("btnAceptarInfo");


function iniciar() {
    events();
}

function events() {
    formulario.addEventListener("submit", function (event) {
        event.preventDefault();
        const cedula = document.getElementById("cedula").value.trim();
        const nombre = document.getElementById("nombre").value.trim();
        const correo = document.getElementById("correo").value.trim();
        const password = document.getElementById("password").value.trim();
        const confirmarPassword = document.getElementById("confirmarPassword").value.trim();

        if (cedula == "" || nombre == "" || correo == "" || password == "" || confirmarPassword == "") {
            openModalInformativo("📪", "Campos vacios", "Faltan datos por llenar");
            return;
        }

        if (password != confirmarPassword) {
            openModalInformativo("❌", "Contraseñas invalidas", "Las contraseñas no coinciden");
            return;
        }

        registro(cedula, nombre, correo, password);
    })


    //Funcion para hacer visible las passwords
    document.querySelectorAll('.password-toggle').forEach(boton_OJO => {
        boton_OJO.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const passwordInput = document.getElementById(targetId);

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                this.textContent = '🙈'; // Ojo cerrado
            } else {
                passwordInput.type = 'password';
                this.textContent = '👁️'; // Ojo abierto
            }
        });
    });

    btnCerrar.addEventListener("click", function () {
        closeElement(modalInformativo);
    })

    btnAceptar.addEventListener("click", function () {
        closeElement(modalInformativo);
    })
}


async function registro(cedula, nombre, correo, password) {

    try {

        const peticion = await fetch("/registro", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                "cedula": cedula,
                "nombre": nombre,
                "correo": correo,
                "password": password
            })
        })

        const datos = await peticion.json();

        if (datos.success == false) {
            openModalInformativo("❌", datos.titulo, datos.mensaje);
            return;
        }

        openModalInformativo("👍", "usuario agregado", "usuario agregado con exito");

    } catch (error) {
        openModalInformativo("❌", "Error fatal", error);
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

