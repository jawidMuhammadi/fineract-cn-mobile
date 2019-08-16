package org.apache.fineract.data.models.customer

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import kotlinx.android.parcel.Parcelize


/*
 * Created by saksham on 17/August/2019
*/


@Parcelize
data class CustomerTest(
        var identifier: String? = null,
        var type: String? = null,
        var givenName: String? = null,
        var middleName: String? = null,
        var surname: String? = null,
        var dateOfBirth: DateOfBirth? = null,
        var member: Boolean? = null,
        var accountBeneficiary: String? = null,
        var referenceCustomer: String? = null,
        var assignedOffice: String? = null,
        var assignedEmployee: String? = null,
        var address: Address? = null,
        var contactDetails: List<ContactDetail>? = null,
        var currentState: State? = null,
        var createdBy: String? = null,
        var createdOn: String? = null,
        var lastModifiedBy: String? = null,
        var lastModifiedOn: String? = null
) : Parcelable {

    /*enum class Type {

        @SerializedName("PERSON")
        PERSON,

        @SerializedName("BUSINESS")
        BUSINESS
    }*/

    enum class State {

        @SerializedName("PENDING")
        PENDING,

        @SerializedName("ACTIVE")
        ACTIVE,

        @SerializedName("LOCKED")
        LOCKED,

        @SerializedName("CLOSED")
        CLOSED
    }

}