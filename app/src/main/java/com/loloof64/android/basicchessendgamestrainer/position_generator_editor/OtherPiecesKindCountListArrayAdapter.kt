/*
 * Basic Chess Endgames : generates a position of the endgame you want, then play it against computer.
    Copyright (C) 2017-2018  Laurent Bernabe <laurent.bernabe@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.loloof64.android.basicchessendgamestrainer.position_generator_editor

import android.app.Activity
import android.app.AlertDialog
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.PositionGeneratorValuesHolder
import com.loloof64.android.basicchessendgamestrainer.R
import java.lang.ref.WeakReference

enum class PieceType {
    Pawn, Knight, Bishop, Rook, Queen, King;

    fun toLocalString(): String {
        val myApplicationContext = MyApplication.getApplicationContext()
        return if (myApplicationContext == null) "#[ERR]"
        else {
            val pieceTypeStringArray = myApplicationContext.resources.getStringArray(R.array.piece_type_spinner)
            pieceTypeStringArray[ordinal]
        }
    }
}

enum class Side {
    Player, Computer;

    fun toLocalString(): String {
        val myApplicationContext = MyApplication.getApplicationContext()
        return if (myApplicationContext == null) "#[ERR]"
        else {
            val pieceTypeStringArray = myApplicationContext.resources.getStringArray(R.array.player_computer_spinner)
            pieceTypeStringArray[ordinal]
        }
    }
}

data class PieceKind(val pieceType: PieceType, val side: Side) {
    fun toLocalString(): String {
        return "${pieceType.toLocalString()} ${side.toLocalString()}"
    }
}

data class PieceKindCount(val pieceKind: PieceKind, val count: Int){
    init {
        if (count < 1 || count > 10) throw IllegalArgumentException("Count($count) is not in range [1,10]!")
    }
}

class OtherPiecesKindCountListArrayAdapter(activity: Activity) : RecyclerView.Adapter<OtherPiecesKindCountListArrayAdapter.Companion.ViewHolder>() {

    private val activityRef = WeakReference(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.other_pieces_count_list_row, parent, false) as LinearLayout
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        fun getColorFromId(colorId: Int) : Int {
            val context = MyApplication.getApplicationContext()
            return ResourcesCompat.getColor(context!!.resources, colorId, null)
        }

        val resources = MyApplication.appContext.resources
        val item = PositionGeneratorValuesHolder.otherPiecesCount[position]

        holder.countTextView.text = item.count.toString()
        holder.countTextView.setBackgroundColor(getColorFromId(R.color.other_piece_count_color))

        holder.ownerTextView.text = resources.getStringArray(R.array.player_computer_spinner)[item.pieceKind.side.ordinal]
        holder.ownerTextView.setBackgroundColor(getColorFromId(R.color.other_piece_owner_color))

        holder.typeTextView.text = resources.getStringArray(R.array.piece_type_spinner)[item.pieceKind.pieceType.ordinal]
        holder.typeTextView.setBackgroundColor(getColorFromId(R.color.other_piece_type_color))

        holder.deleteButton.setOnClickListener(DeleteButtonClickListener(pieceKind = item.pieceKind, activityRef = activityRef, adapter = this, position = position))
    }

    override fun getItemCount(): Int {
        return PositionGeneratorValuesHolder.otherPiecesCount.size
    }

    fun deleteItem(position: Int) {
        PositionGeneratorValuesHolder.otherPiecesCount.removeAt(position)
        notifyDataSetChanged()
    }

    fun tryToAddPieceCount(currentDefinedPieceCount: PieceKindCount) {
        val notSetYetForThisPieceKind =
                PositionGeneratorValuesHolder.otherPiecesCount.none {
                    it.pieceKind == currentDefinedPieceCount.pieceKind
                }
        if (notSetYetForThisPieceKind) {
            val tooManyQueens = currentDefinedPieceCount.pieceKind.pieceType == PieceType.Queen &&
                    currentDefinedPieceCount.count > 9
            val tooManyPawns = currentDefinedPieceCount.pieceKind.pieceType == PieceType.Pawn &&
                    currentDefinedPieceCount.count > 8
            val futurePieceCountState = PositionGeneratorValuesHolder.otherPiecesCount.toMutableList()
            futurePieceCountState.add(currentDefinedPieceCount)
            val playerPiecesCount = futurePieceCountState.filter { it.pieceKind.side == Side.Player }.map { it.count }.sum()
            val computerPiecesCount = futurePieceCountState.filter { it.pieceKind.side == Side.Computer }.map { it.count }.sum()

            val tooManyPieces = playerPiecesCount > 15 || computerPiecesCount > 15
            when {
                tooManyQueens -> Toast.makeText(MyApplication.appContext, R.string.adding_too_many_queens, Toast.LENGTH_LONG).show()
                tooManyPawns -> Toast.makeText(MyApplication.appContext, R.string.adding_too_many_pawns, Toast.LENGTH_LONG).show()
                tooManyPieces -> Toast.makeText(MyApplication.appContext, R.string.adding_too_many_pieces, Toast.LENGTH_LONG).show()
                else -> {
                    PositionGeneratorValuesHolder.otherPiecesCount.add(currentDefinedPieceCount)
                    notifyDataSetChanged()
                }
            }
        } else {
            Toast.makeText(MyApplication.appContext, R.string.piece_kind_already_in_list, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val deleteButton: ImageButton = view.findViewById(R.id.delete_piece_kind_count)
            val countTextView:TextView = view.findViewById(R.id.text_view_piece_kind_count)
            val ownerTextView:TextView = view.findViewById(R.id.text_view_piece_kind_owner)
            val typeTextView: TextView = view.findViewById(R.id.text_view_piece_kind_type)
        }

        private const val PawnCode = "P"
        private const val KnightCode = "N"
        private const val BishopCode = "B"
        private const val RookCode = "R"
        private const val QueenCode = "Q"

        private const val pieceCountLineSeparator = "@"

        fun getPiecesCountFromString(piecesCountString: String): List<PieceKindCount> {

            fun lineToPieceCount(lineSpec: String): PieceKindCount {

                val parts = lineSpec.trim().split(" ")
                val side = if (parts[0] == "C") Side.Computer else Side.Player
                val type = when(parts[1]) {
                    PawnCode -> PieceType.Pawn
                    KnightCode -> PieceType.Knight
                    BishopCode -> PieceType.Bishop
                    RookCode -> PieceType.Rook
                    QueenCode -> PieceType.Queen
                    else -> throw RuntimeException("This piece kind code (${parts[1]}) cannot be accepted.")
                }
                val count = Integer.parseInt(parts[2])

                val tooManyQueens = type == PieceType.Queen && count > 9
                val tooManyPawns = type == PieceType.Pawn && count > 8

                if (tooManyPawns) throw RuntimeException("There must be at least 8 pawns per side !")
                if (tooManyQueens) throw RuntimeException("There must be at least 9 queens per side !")

                return PieceKindCount(pieceKind = PieceKind(pieceType = type, side = side), count = count)
            }

            val listToReturn = mutableListOf<PieceKindCount>()
            piecesCountString.split(pieceCountLineSeparator).filter { it.isNotEmpty() }.map { lineToPieceCount(it) }.forEach { kindCount ->
                if (listToReturn.none { it.pieceKind == kindCount.pieceKind }) listToReturn.add(kindCount)
            }

            val playerPiecesCount = listToReturn.filter { it.pieceKind.side == Side.Player }.map { it.count }.sum()
            val computerPiecesCount = listToReturn.filter { it.pieceKind.side == Side.Computer }.map { it.count }.sum()

            if (playerPiecesCount > 16) throw RuntimeException("There is too many pieces for player ($playerPiecesCount > 16) !")
            if (computerPiecesCount > 16) throw RuntimeException("There is too many pieces for computer ($computerPiecesCount > 16) !")

            return listToReturn.toList()
        }

        fun stringFromPiecesCount(piecesCount: List<PieceKindCount>): String {
            fun pieceKindToString(kindCount: PieceKindCount): String {
                val sideString = if (kindCount.pieceKind.side == Side.Player) "P" else "C"
                val typeString = when (kindCount.pieceKind.pieceType) {
                    PieceType.Pawn -> PawnCode
                    PieceType.Knight -> KnightCode
                    PieceType.Bishop -> BishopCode
                    PieceType.Rook -> RookCode
                    PieceType.Queen -> QueenCode
                    else -> throw RuntimeException("This piece type (${kindCount.pieceKind.pieceType} cannot be accepted.")
                }
                val count = kindCount.count.toString()

                return "$sideString $typeString $count"
            }


            val strBuilder = StringBuilder()

            piecesCount.forEach {
                strBuilder.append(pieceKindToString(it))
                strBuilder.append(pieceCountLineSeparator)
            }

            return strBuilder.toString()
        }
    }

}

class DeleteButtonClickListener(val pieceKind: PieceKind,
                                adapter: OtherPiecesKindCountListArrayAdapter,
                                val activityRef: WeakReference<Activity>,
                                val position: Int) : View.OnClickListener {
    private val adapterRef = WeakReference(adapter)

    override fun onClick(view: View?) {
        if (adapterRef.get() != null && view != null) {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog(){
        val alertDialogBuilder = AlertDialog.Builder(activityRef.get()).create()
        alertDialogBuilder.setTitle(R.string.confirm_delete_piece_kind_count_title)
        alertDialogBuilder.setMessage(activityRef.get()?.resources?.getString(R.string.confirm_delete_piece_kind_count_message, pieceKind.toLocalString()))
        alertDialogBuilder.setButton(AlertDialog.BUTTON_POSITIVE, activityRef.get()?.resources?.getString(R.string.OK), {
            _, _ ->
            adapterRef.get()!!.deleteItem(position)
            OtherPiecesGlobalConstraintEditorFragment.deleteScriptAssociatedWithPieceKind(pieceKind)
        })
        alertDialogBuilder.setButton(AlertDialog.BUTTON_NEGATIVE, activityRef.get()?.resources?.getString(R.string.cancel), {
            dialog, _ -> dialog.dismiss()
        })
        alertDialogBuilder.show()
    }

}