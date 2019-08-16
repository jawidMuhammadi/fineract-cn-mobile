package org.apache.fineract.ui.online.customers.createcustomer.customeractivity;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

import org.apache.fineract.R;
import org.apache.fineract.data.couchbasesync.SynchronizationManager;
import org.apache.fineract.data.datamanager.contracts.ManagerCustomer;
import org.apache.fineract.data.datamanager.database.DbManagerCustomer;
import org.apache.fineract.data.models.accounts.AccountType;
import org.apache.fineract.data.models.customer.ContactDetail;
import org.apache.fineract.data.models.customer.Customer;
import org.apache.fineract.data.models.customer.CustomerTest;
import org.apache.fineract.data.models.customer.DateOfBirth;
import org.apache.fineract.data.models.deposit.DepositAccount;
import org.apache.fineract.data.models.teller.Teller;
import org.apache.fineract.injection.ApplicationContext;
import org.apache.fineract.injection.ConfigPersistent;
import org.apache.fineract.ui.base.BasePresenter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Rajan Maurya
 * On 27/07/17.
 */
@ConfigPersistent
public class CreateCustomerPresenter extends BasePresenter<CreateCustomerContract.View>
        implements CreateCustomerContract.Presenter {

    private ManagerCustomer dbManagerCustomer;
    private final CompositeDisposable compositeDisposable;

    @Inject
    public CreateCustomerPresenter(@ApplicationContext Context context,
                                   DbManagerCustomer dataManagerCustomer) {
        super(context);
        this.dbManagerCustomer = dataManagerCustomer;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void createCustomer(final Customer customer) {
        checkViewAttached();
        Log.d("TAG", "showing progress bar");
        getMvpView().showProgressbar();

        Log.d("TAG", "after showing progress bar 1");

        HashMap<String, CustomerTest> hm = new HashMap<>();
        Log.d("TAG", "after showing progress bar 2");

        CustomerTest customerTest = new CustomerTest("iden", "iden", "iden", "iden", "iden", null, true, "iden", "iden", "iden", "iden", null, null,
                CustomerTest.State.ACTIVE, "iden", "iden", "iden", "iden");
        Log.d("TAG", "after showing progress bar 3");
        hm.put("key", customerTest);


        try {
            Log.d("TAG", "after showing progress bar 4");
            SynchronizationManager.getSynchronizationManager().createDocument(hm);
            Log.d("TAG", "after showing progress bar 5");
        } catch (Exception cle) {
            getMvpView().showError(context.getString(R.string.error_creating_customer));
            getMvpView().hideProgressbar();
            return;
        }
        getMvpView().hideProgressbar();
        getMvpView().customerCreatedSuccessfully();
    }

    @Override
    public void updateCustomer(String customerIdentifier, Customer customer) {
        checkViewAttached();
        getMvpView().showProgressbar();
        compositeDisposable.add(dbManagerCustomer.updateCustomer(customerIdentifier, customer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        getMvpView().hideProgressbar();
                        getMvpView().customerUpdatedSuccessfully();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        getMvpView().hideProgressbar();
                        showExceptionError(throwable,
                                context.getString(R.string.error_updating_customer));
                    }
                })
        );
    }
}
