package ru.you11.prototypechattestapp.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import ru.you11.prototypechattestapp.Message
import ru.you11.prototypechattestapp.R
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

class ChatFragment: Fragment(), Contract.Chat.View {

    override lateinit var presenter: Contract.Chat.Presenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInputView: EditText
    private lateinit var sendButton: Button
    private val messages = ArrayList<Message>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        with(root) {
            recyclerView = findViewById(R.id.messages_rv)
            recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                recyclerView.scrollToPosition(messages.size - 1)
            }
            
            messageInputView = findViewById(R.id.message_bar_edit_text)
            messageInputView.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    sendButton.isEnabled = text != null && text.isNotBlank()
                }
            })
            messageInputView.setOnEditorActionListener { _, _, _ ->
                if (messageInputView.text.isNotBlank()) {
                    sendMessage()
                    true
                } else {
                    false
                }
            }

            sendButton = findViewById(R.id.message_bar_send_button)

            sendButton.setOnClickListener {
                if (messageInputView.text.isNotBlank()) {
                    sendMessage()
                }
            }

            setupRecyclerView()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_chat, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign_out_user -> {
                presenter.signOutUser()
                true
            }

            else -> {
                false
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MessengerRVAdapter(messages)
    }

    override fun showMessages(messages: ArrayList<Message>) {
        this.messages.clear()
        this.messages.addAll(messages)
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(messages.size - 1)

    }

    override fun showReceiveMessagesError(exception: Exception?) {
        if (exception != null) {
            Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, resources.getString(R.string.unknown_error_text), Toast.LENGTH_SHORT).show()
        }
    }

    override fun showSendMessageError(exception: Exception?) {
        if (exception != null) {
            Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, resources.getString(R.string.send_message_error_text), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        presenter.sendMessage(messageInputView.text.toString())
        messageInputView.text.clear()
    }

    override fun onMessageSent() {
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }


    class MessengerRVAdapter(private val messages: ArrayList<Message>): RecyclerView.Adapter<MessengerRVAdapter.ViewHolder>() {

        class ViewHolder(val layout: LinearLayout): RecyclerView.ViewHolder(layout) {
            val message: TextView = layout.findViewById(R.id.message_text)
            val sender: TextView = layout.findViewById(R.id.message_sender)
            val sentDate: TextView = layout.findViewById(R.id.message_sent_date)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false) as LinearLayout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.message.text = messages[position].content
            holder.sender.text = messages[position].sender.name
            holder.sender.setTextColor(messages[position].sender.color)

            val sdt = SimpleDateFormat("HH:mm dd-MM-yy", Locale.getDefault())

            holder.sentDate.text = sdt.format(messages[position].sendDate)
        }

        override fun getItemCount(): Int {
            return messages.size
        }
    }
}