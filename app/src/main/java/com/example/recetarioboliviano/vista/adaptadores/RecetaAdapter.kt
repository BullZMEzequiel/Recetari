package com.example.recetarioboliviano.vista.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.databinding.ItemRecetaBinding
import com.example.recetarioboliviano.modelo.entidades.Receta
import coil.load
import coil.transform.RoundedCornersTransformation

class RecetaAdapter(
    private val onItemClick: (Receta) -> Unit,
    private val onFavoritoClick: (Receta) -> Unit
) : ListAdapter<Receta, RecetaAdapter.RecetaViewHolder>(RecetaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val binding = ItemRecetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecetaViewHolder(
        private val binding: ItemRecetaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.btnFavorito.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoritoClick(getItem(position))
                }
            }
        }

        fun bind(receta: Receta) {
            binding.tvTitulo.text = receta.titulo
            binding.tvDepartamento.text = receta.departamento
            binding.tvTiempo.text = receta.tiempoPreparacion
            binding.tvCategoria.text = receta.categoria

            val iconRes = if (receta.esFavorito) {
                R.drawable.ic_favorite
            } else {
                R.drawable.ic_favorite_border
            }
            binding.btnFavorito.setIconResource(iconRes)

            binding.chipUsuario.visibility =
                if (receta.esCreadaPorUsuario) View.VISIBLE else View.GONE

            // Manejo dinámico del origen de la imagen para soportar la caché e imágenes locales
            gestionarCargaDeImagen(binding.root.context, binding.ivReceta, receta.imagenUri)
        }

        /**
         * Determina si el uri es un enlace web, un archivo local o un recurso estático (drawable)
         */
        private fun gestionarCargaDeImagen(context: Context, imageView: ImageView, uri: String?) {
            if (uri.isNullOrEmpty()) {
                imageView.load(R.drawable.ic_image_placeholder) {
                    transformations(RoundedCornersTransformation(16f))
                }
                return
            }

            if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("content://") || uri.startsWith("file") || uri.startsWith("/")) {
                // CASO 1: Internet (Viene de tu panel CRUD en VS Code) o Archivo de Cámara/Galería
                // Coil guarda automáticamente en caché de disco las URLs HTTP sin configurar nada extra
                imageView.load(uri) {
                    crossfade(true)
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_image_placeholder)
                    transformations(RoundedCornersTransformation(16f))
                }
            } else {
                // CASO 2: Receta base oficial (El String es el nombre puro de la foto, ej: "img_fricase")
                // Buscamos el ID numérico dinámicamente en la carpeta drawable
                val resourceId = context.resources.getIdentifier(
                    uri,
                    "drawable",
                    context.packageName
                )

                if (resourceId != 0) {
                    imageView.load(resourceId) {
                        crossfade(true)
                        transformations(RoundedCornersTransformation(16f))
                    }
                } else {
                    imageView.load(R.drawable.ic_image_placeholder) {
                        transformations(RoundedCornersTransformation(16f))
                    }
                }
            }
        }
    }

    class RecetaDiffCallback : DiffUtil.ItemCallback<Receta>() {
        override fun areItemsTheSame(oldItem: Receta, newItem: Receta): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Receta, newItem: Receta): Boolean {
            return oldItem == newItem
        }
    }
}