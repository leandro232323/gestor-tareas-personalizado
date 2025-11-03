
//Modal general
const modalInformativo = document.getElementById("modal-general");
const tituloModalInformativo = document.getElementById("modalTitulo");
const mensajeModalInformativo = document.getElementById("modalMensaje");
const btnmodal_Cerrar_General = document.getElementById("btnCerrarInfo");
const btnmodal_Aceptar_General = document.getElementById("btnAceptarInfo");
const modalInformativo_iconoError = document.getElementById('simbolo');


const cedulaSesionUsuario = localStorage.getItem('cedulaUsuario');

let spanLimite = document.getElementById('span-limite');
let tablaActual = '';
let nombre = '';

// Array con todos los IDs de botones
const botonesAbrir = [
    'btnAbrirLunes', 'btnAbrirMartes', 'btnAbrirMiercoles',
    'btnAbrirJueves', 'btnAbrirViernes', 'btnAbrirSabado', 'btnAbrirDomingo'
];

function iniciar() {
    console.log(cedulaSesionUsuario);
    eventos();
    mostrarTareasUser();
}


async function mostrarTareasUser(){
    try {
        const peticion = await fetch("/mostrarTareas", {
            method: "GET",
            headers: {
                'Content-Type' : 'application/json',
                "cedula" : cedulaSesionUsuario
            }
        });

        const datos = await peticion.json();
        if(datos.estado == true){
            console.log(datos.mensaje)
            return
        }
    } catch (error) {
        openModalInformativo("❌", "Error fatal", error.message);
    }
}

function eventos() {
    try {
        // Verificar que los elementos existan antes de agregar event listeners
        const btnCerrarModal = document.getElementById('btnCerrar');
        const modal = document.getElementById('modal');
        const botonAdd_tarea = document.getElementById("btnGuardar_modal");
        const inputNombre = document.getElementById('taskName');
        const spanLimite = document.getElementById('span-limite');


        // Configurar botones de abrir modal
        botonesAbrir.forEach(id => {
            const boton = document.getElementById(id);
            if (boton) {
                boton.addEventListener("click", function () {
                    openElement(modal);
                });
            }
        });

        // Event listener para cerrar modales
        document.addEventListener("click", (event) => {
            const tabla = event.target.closest('.table')?.id;
            if (tabla) {
                tablaActual = tabla;
                console.log(tablaActual);
            }

            const btnCerrar = event.target.closest('#btnCerrar');
            const btnCerrarInformacion = event.target.closest('#btnCerrarInfo');
            const btnCerrarAceptarInformacion = event.target.closest('#btnAceptarInfo');

            if (btnCerrar) {
                closeElement(modal);
                clean_modal_informativo();
            }

            if (btnCerrarInformacion || btnCerrarAceptarInformacion) {
                closeElement(modalInformativo);
                clean_modal_informativo();
            }
        });

        // Event listener para agregar tarea
        if (botonAdd_tarea) {
            botonAdd_tarea.addEventListener('click', async () => {
                try {
                    let nombre = document.getElementById('taskName').value;
                    let materia = document.getElementById('subject').value;
                    let accion = document.getElementById('description').value;
                    let fecha_Entrega = document.getElementById('dueDate').value;

                    if (nombre === "" || materia === "" || accion === "" || fecha_Entrega === "") {
                        openModalInformativo("📪", "Campos vacios", "Faltan datos por llenar");
                        return;
                    }


                    //Inserccion en la base de datos
                    const peticion = await fetch("/agregarTarea", {
                        method: "POST",
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            "nombre_tarea": nombre,
                            "nombre_materia": materia,
                            "accion_tarea": accion,
                            "fecha": fecha_Entrega,
                            "tabla_actual": tablaActual,
                            "cedula_seccion": cedulaSesionUsuario
                        })
                    })

                    const datos = await peticion.json();
                    if(datos.estado == false){
                        openModalInformativo("❌", datos.titulo, datos.mensaje);
                        return;
                    }

                    console.log("tarea agregada con exito");

                    const elemento_tablaActual = document.createElement("div");
                    const tabla = document.getElementById(tablaActual);
                    elemento_tablaActual.className = "cuerpo";
                    elemento_tablaActual.innerHTML = `
                        <div class="celda-cuerpo">
                            <div class="contenido-celda">${nombre}</div>
                        </div>

                        <div class="content-btn">
                            <button class="btn-ver">ver</button>
                        </div>
                    `;

                    if (tabla) {
                        tabla.appendChild(elemento_tablaActual);
                        closeElement(modal);
                        clean_modal_informativo();
                    }

                } catch (error) {
                    openModalInformativo("❌", "Error al agregar la tarea en la tabla", error.message);
                }
            });
        }

        // Event listener para el input de nombre
        if (inputNombre && spanLimite) {
            inputNombre.addEventListener('input', function () {
                let texto = this.value;
                spanLimite.textContent = texto.length;
                if (texto.length > 29) {
                    openModalInformativo("⚠️", "Limite de caracteres", "El campo (nombre de la tarea) no debe ser mayor a 29 caracteres");
                    return;
                }
            });
        }

    } catch (error) {
        console.error("Error en eventos:", error);
        openModalInformativo("❌", "Error fatal", error.message);
    }
}

window.addEventListener("DOMContentLoaded", iniciar);


function openModalInformativo(icono, titulo, mensaje) {
    modalInformativo_iconoError.textContent = icono;
    tituloModalInformativo.textContent = titulo;
    mensajeModalInformativo.textContent = mensaje;
    openElement(modalInformativo);
}


function clean_modal_informativo() {
    let nombre = document.getElementById('taskName').value = "";
    let materia = document.getElementById('subject').value = "";
    let accion = document.getElementById('description').value = "";
    let fecha_Entrega = document.getElementById('dueDate').value = "";
    spanLimite.textContent = "0";
}


function openElement(elemento) {
    elemento.classList.remove("noMostrar");
    elemento.classList.add("mostrar");
}

function closeElement(elemento) {
    elemento.classList.remove("mostrar");
    elemento.classList.add("noMostrar");
}




