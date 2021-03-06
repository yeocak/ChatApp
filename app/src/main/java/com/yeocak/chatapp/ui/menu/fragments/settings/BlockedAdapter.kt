package com.yeocak.chatapp.ui.menu.fragments.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.yeocak.chatapp.utils.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.SingleBlockedUserBlockBinding

class BlockedAdapter (
    private val blockedListName: MutableList<String>, private val blockedListId: MutableList<String>
        ) : RecyclerView.Adapter<BlockedAdapter.BlockedViewHolder>() {

    class BlockedViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = SingleBlockedUserBlockBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        return BlockedViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_blocked_user_block,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val currentName = blockedListName[position]
        val currentId = blockedListId[position]

        with(holder.binding){
            tvBlockedUserName.text = currentName

            btnUnblockUser.setOnClickListener {
                val db = FirebaseFirestore.getInstance()

                db.collection("block").document(userUID!!).collection("to").document(currentId).delete()
                db.collection("block").document(currentId).collection("from").document(userUID!!).delete()

                blockedListName.removeAt(position)
                blockedListId.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return blockedListName.size
    }

}