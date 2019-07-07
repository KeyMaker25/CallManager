package bernat.caller.models

class Contact(var number: String,var name: String) {

    override fun toString(): String {
        return "name: "+this.name+" number: "+this.number
    }
}