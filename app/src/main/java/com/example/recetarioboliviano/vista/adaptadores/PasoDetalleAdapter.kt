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
import com.example.recetarioboliviano.databinding.ItemPasoDetalleBinding
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import coil.load

/**
 * Adaptador para mostrar los pasos de preparación en la pantalla de detalle.
 */
class PasoDetalleAdapter : ListAdapter<PasoPreparacion, PasoDetalleAdapter.PasoViewHolder>(PasoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasoViewHolder {
        val binding = ItemPasoDetalleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PasoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PasoViewHolder(
        private val binding: ItemPasoDetalleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(paso: PasoPreparacion) {
            binding.tvNumeroPaso.text = paso.numero.toString()
            binding.tvDescripcionPaso.text = paso.descripcion

            if (!paso.imagenUri.isNullOrEmpty()) {
                binding.cardImagenPaso.visibility = View.VISIBLE
                // CARGA HÍBRIDA OPTIMIZADA CON COIL PARA LOS PASOS
                gestionarCargaImagenPaso(binding.root.context, binding.ivImagenPaso, paso.imagenUri)
            } else {
                binding.cardImagenPaso.visibility = View.GONE
            }
        }

        private fun gestionarCargaImagenPaso(context: Context, imageView: ImageView, uri: String) {
            if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("content://") || uri.startsWith("/")) {
                // CASO A: URL de Internet (Cloudinary/Panel) o foto local de la cámara del usuario
                imageView.load(uri) {
                    crossfade(true)
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_image_placeholder)
                }
            } else {
                // CASO B: Receta del sistema que apunte a un drawable interno por nombre de texto
                val resourceId = context.resources.getIdentifier(
                    uri,
                    "drawable",
                    context.packageName
                )
                if (resourceId != 0) {
                    imageView.load(resourceId) {
                        crossfade(true)
                    }
                } else {
                    imageView.load(R.drawable.ic_image_placeholder)
                }
            }
        }
    }

    class PasoDiffCallback : DiffUtil.ItemCallback<PasoPreparacion>() {
        override fun areItemsTheSame(oldItem: PasoPreparacion, newItem: PasoPreparacion): Boolean {
            return oldItem.numero == newItem.numero
        }

        override fun areContentsTheSame(oldItem: PasoPreparacion, newItem: PasoPreparacion): Boolean {
            return oldItem == newItem
        }
    }
}
