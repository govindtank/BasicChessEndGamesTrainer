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

package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.R

interface ItemLongClickListener {
    fun onLongClick(position: Int)
}

class CustomExercisesListAdapter(private val itemClickListener: ItemClickListener,
                                 private val itemLongClickListener: ItemLongClickListener)
    : RecyclerView.Adapter<CustomExercisesListAdapter.Companion.ViewHolder>(){

    private var exercisesList: List<ExerciseInfo> = listOf()

    companion object {
        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent?.context).inflate(R.layout.exercises_list_row, parent, false) as LinearLayout
        val textView = layout.findViewById<TextView>(R.id.exercise_list_row_value)
        layout.removeView(textView)
        return CustomExercisesListAdapter.Companion.ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        fun getColor(colorId: Int) : Int {
            val context = MyApplication.getApplicationContext()
            return ResourcesCompat.getColor(context.resources, colorId, null)
        }

        holder?.textView?.text = MyApplication.getApplicationContext().getString(exercisesList[position].textId)
        holder?.textView?.setOnClickListener{ itemClickListener.onClick(position) }
        holder?.textView?.setOnLongClickListener { itemLongClickListener.onLongClick(position); true }
        holder?.textView?.setBackgroundColor(
                if (exercisesList[position].mustWin) getColor(R.color.exercise_chooser_winning_color)
                else getColor(R.color.exercise_chooser_nullifying_color)
        )
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }

}