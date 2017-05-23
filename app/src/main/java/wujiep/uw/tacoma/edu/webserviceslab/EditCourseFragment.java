package wujiep.uw.tacoma.edu.webserviceslab;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import wujiep.uw.tacoma.edu.webserviceslab.course.Course;
import wujiep.uw.tacoma.edu.webserviceslab.data.CourseDB;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditCourseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditCourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCourseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private final static String COURSE_EDIT_URL
            = "http://cssgate.insttech.washington.edu/~wujiep/Android/editCourse.php?";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TextView mCourseIdTextView;
    private EditText mCourseShortDescEditView;
    private EditText mCourseLongDescEditView;
    private EditText mCoursePrereqsEditView;

    private CourseActivity mActivity;

    private OnFragmentInteractionListener mListener;
    private CourseDB mCourseDB;
    private List<Course> mCourseList;

    public EditCourseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditCourseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCourseFragment newInstance(String param1, String param2) {
        EditCourseFragment fragment = new EditCourseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_course, container, false);

        mCourseIdTextView = (TextView) view.findViewById(R.id.edit_course_item_id);
        mCourseShortDescEditView = (EditText) view.findViewById(R.id.edit_course_short_desc);
        mCourseLongDescEditView = (EditText) view.findViewById(R.id.edit_course_long_desc);
        mCoursePrereqsEditView = (EditText) view.findViewById(R.id.edit_course_prereqs);

        mCourseIdTextView.setText(getArguments().getString(Course.ID));
        mCourseShortDescEditView.setText(getArguments().getString(Course.SHORT_DESC));
        mCourseLongDescEditView.setText(getArguments().getString(Course.LONG_DESC));
        mCoursePrereqsEditView.setText(getArguments().getString(Course.PRE_REQS));



        Button makeChangesButton = (Button) view.findViewById(R.id.make_changes_button);
        makeChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCourseDB == null) {
                    mCourseDB = new CourseDB(getActivity());
                }
                boolean result = mCourseDB.updateCourse(mCourseIdTextView.getText().toString(), mCourseShortDescEditView.getText().toString(),
                        mCourseLongDescEditView.getText().toString(), mCoursePrereqsEditView.getText().toString());
                Toast.makeText(v.getContext(), "Local database update: " + result , Toast.LENGTH_LONG)
                        .show();
                String url = buildEditCourseURL(v);
//                mListener.addCourse(url);

                EditCourseTask task = new EditCourseTask();
                task.execute(new String[]{url.toString()});

                // Takes you back to the previous fragment by popping the current fragment out.
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        return view;
    }


    private String buildEditCourseURL(View v) {

        StringBuilder sb = new StringBuilder(COURSE_EDIT_URL);

        try {

            String courseId = mCourseIdTextView.getText().toString();
            sb.append("id=");
            sb.append(courseId);


            String courseShortDesc = mCourseShortDescEditView.getText().toString();
            sb.append("&shortDesc=");
            sb.append(URLEncoder.encode(courseShortDesc, "UTF-8"));


            String courseLongDesc = mCourseLongDescEditView.getText().toString();
            sb.append("&longDesc=");
            sb.append(URLEncoder.encode(courseLongDesc, "UTF-8"));

            String coursePrereqs = mCoursePrereqsEditView.getText().toString();
            sb.append("&prereqs=");
            sb.append(URLEncoder.encode(coursePrereqs, "UTF-8"));

            Log.i("CourseEditFragment", sb.toString());

        }
        catch(Exception e) {
            Toast.makeText(v.getContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
        return sb.toString();
    }




    @Override
    public void onStart() {

        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();

        if (args != null) {
            // Set article based on argument passed in
            updateView((Course) args.getSerializable(CourseDetailFragment.COURSE_ITEM_SELECTED));
        }
    }

    public void updateView(Course course) {
        if (course != null) {
            mCourseIdTextView.setText(course.getCourseId());
            mCourseShortDescEditView.setText(course.getShortDescription());
            mCourseLongDescEditView.setText(course.getLongDescription());
            mCoursePrereqsEditView.setText(course.getPreReqs());
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof CourseAddFragment.CourseAddListener) {
//            mListener = (CourseAddFragment.CourseAddListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement CourseAddListener");
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.e("THIS IS THE CONTEXT", context.toString());

        if(context instanceof CourseActivity) {
            mActivity = (CourseActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    private class EditCourseTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "Unable to add course, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }


        /**
         * It checks to see if there was a problem with the URL(Network) which is when an
         * exception is caught. It tries to call the parse Method and checks to see if it was successful.
         * If not, it displays the exception.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");

                if (status.equals("success")) {
                    Toast.makeText(mActivity.getApplicationContext(), "Course successfully edited!"
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(mActivity.getApplicationContext(), "Failed to edit: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(mActivity.getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}