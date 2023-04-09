import React, {useEffect, useState} from 'react';
import Banner from '@site/src/components/Banner';
import validator from "node-email-validation";
import Modal from "../utils/Modal";

let IS_PROD = process.env.NODE_ENV === "development";
const SERVER_URL = IS_PROD ? "http://localhost:5003" : "https://api.touchlab.dev"

export default function TrialSignup() {
    function fieldEditFactory(setMethod) {
        return event => {
            console.log("hello", event.target.value)
            setMethod(event.target.value);
        }
    }

    const [orgName, setOrgName] = useState("");
    const [contactName, setContactName] = useState("");
    const [contactEmail, setContactEmail] = useState("");
    const [description, setDescription] = useState("");
    const [trialKey, setTrialKey] = useState("");
    const [errorMessage, setErrorMessage] = useState(null);
    const [checked, setChecked] = useState(false);
    const [termsModalOpen, setTermsModalOpen] = useState(false);

    const showTerms = (e)=>{
        e = e || window.event;
        e.preventDefault();
        setTimeout(()=>setTermsModalOpen(true), 100)

    }
    const handleChange = () => {
        setChecked(!checked);
    };

    const [sending, setSending] = useState(false);

    useEffect(() => {
        try {
            const urlSearchParams = new URLSearchParams(window.location.search);
            const tk = urlSearchParams.get("trialKey")
            if (tk.trim().length > 0)
                setTrialKey(tk)
        } catch (e) {
            console.log(e)
            setErrorMessage("Valid trial request url required. Please check the source of this URL and try again.")
        }
    }, [])

    const okToEdit = trialKey.trim().length > 0
    const okToSend = okToEdit && !sending &&
        orgName.trim().length > 0 &&
        contactName.trim().length > 0 &&
        checked &&
        contactEmail.trim().length > 0 && validator.is_email_valid(contactEmail)

    const requestTrial = (e) => {
        e = e || window.event;
        e.preventDefault();

        if(!okToSend)
            return;

        setErrorMessage(null)

        if (orgName.trim().length === 0 || contactName.trim().length === 0 || contactEmail.trim().length === 0) {
            setErrorMessage(`All required fields need values`)
            return
        }
        if (!validator.is_email_valid(contactEmail)) {
            setErrorMessage(`Email format is invalid ${contactEmail}`)
            return
        }

        setSending(true)
        sendRequest().then(() => {
            setSending(false)
        })
    }

    async function sendRequest() {
        const callUrl = `/touchlabpro/signup`
        try {
            const response = await (await fetch(`${SERVER_URL}${callUrl}`, {
                cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                mode: 'cors',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    orgName: orgName,
                    contactName: contactName,
                    contactEmail: contactEmail,
                    description: description,
                    keyCode: trialKey
                })
            })).json()
            console.log("response", response)
            if (!response.success) {
                setErrorMessage(response.message)
            } else {
                location = "/trialthanks"
                // redirect("/");
                // location = "/trialthanks"
            }
        } catch (error) {
            console.log("error", error)
            setErrorMessage("Server error. Please contact Touchlab for support at info@touchlab.co.")
        }
    }

    const errorBannerOpen = errorMessage !== null;

    return (
        <>
            <Banner className={'fixed top-16 left-0 w-full banner-extra z-30'} type="error" open={errorBannerOpen}
                    clearBanner={() => setErrorMessage(null)}>
                {errorMessage}
            </Banner>

            {okToEdit &&
                <section>
                    <div className="max-w-xl mx-auto px-4 sm:px-6 pt-8 pb-16">
                        <div className="mb-4">
                            <label className="block text-xl font-medium mb-1" htmlFor="card-nr">Organization Name <span
                                className="text-rose-500">*</span></label>
                            <input className="form-input w-full" type="text"
                                   value={orgName}
                                   onChange={fieldEditFactory(setOrgName)}

                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-xl font-medium mb-1" htmlFor="card-nr">Contact Name <span
                                className="text-rose-500">*</span></label>
                            <input className="form-input w-full" type="text"
                                   value={contactName}
                                   onChange={fieldEditFactory(setContactName)}
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-xl font-medium mb-1" htmlFor="card-nr">Contact Email <span
                                className="text-rose-500">*</span></label>
                            <input className="form-input w-full" type="email"
                                   value={contactEmail}
                                   onChange={fieldEditFactory(setContactEmail)}
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-xl font-medium mb-1" htmlFor="card-nr">Any more details you'd
                                like to share?</label>
                            <textarea className="form-input w-full"
                                      value={description}
                                      onChange={fieldEditFactory(setDescription)}
                            />
                        </div>
                        <div className="mb-4">

                            <label className="block text-xl font-medium mb-1" htmlFor="card-nr">Accept The <a
                                href="" onClick={showTerms}>Software
                                Evaluation License Agreement</a></label>
                            <input
                                className="bg-slate-400"
                                type="checkbox"
                                checked={checked}
                                onChange={handleChange}
                            />
                            <Modal id="modal" ariaLabel="modal-headline" show={termsModalOpen} handleClose={() => {setTermsModalOpen(false)}}>
                                <div className="relative p-8 overflow-scroll h-full">
<br/>
                                    THIS END USER SOFTWARE EVALUATION LICENSE AGREEMENT (THIS "AGREEMENT") IS BETWEEN TOUCH LAB EVENTS LLC, D/B/A TOUCHLAB AND YOU. BY CLICKING ON THE "I AGREE" BUTTON, YOU ACKNOWLEDGE THAT YOU HAVE READ, UNDERSTOOD AND AGREE TO ALL OF THE TERMS AND CONDITIONS SET FORTH BELOW. IF YOU DO NOT AGREE TO ANY OF THE TERMS AND CONDITIONS OF THIS AGREEMENT, TOUCH LAB EVENTS LLC IS UNWILLING TO LICENSE THE SOFTWARE TO YOU, AND YOU MUST NOT INSTALL THE SOFTWARE. THE "EFFECTIVE DATE" OF THIS AGREEMENT IS THE DATE UPON WHICH YOU CLICK THE "I AGREE" BUTTON. For the purpose of this Agreement, you and, if applicable, such company (or other entity) constitutes "you", "your" or "Customer" (“Licensee”) and "Touch Lab Events LLC, d/b/a Touchlab", "us", "our" or "we" refers to Touch Lab Events LLC, d/b/a Touchlab (“Licensor”), 1216 Broadway, and its Affiliates, the owner and provider of the Software.<br/>
<br/>
                                    DEFINITIONS<br/>
                                    1.1 "Effective Date" means the date  IS THE DATE UPON WHICH YOU CLICK THE "I AGREE" BUTTON when you the Customer accepts the terms and conditions of this Agreement.<br/>
<br/>
                                    1.2 "Software" means the software provided by the Licensor, which includes computer software and associated media, printed materials, and electronic documentation.<br/>
<br/>
                                    1.3 "Evaluation License" means the limited, non-exclusive, non-transferable, revocable license to use the Software during the Evaluation Period.<br/>
<br/>
                                    1.4 "Evaluation Period" means the time period specified by the Licensor, during which the Licensee may use the Software for evaluation purposes only.<br/>
<br/>
                                    EVALUATION LICENSE<br/>
                                    2.1 Subject to the terms and conditions of this Agreement, the Licensor grants to the Licensee a Evaluation License to install, execute, and use the Software solely for internal evaluation purposes during the Evaluation Period.<br/>
<br/>
                                    2.2 The Licensee may not use the Software for commercial, revenue-generating, or other non-evaluation purposes.<br/>
<br/>
                                    RESTRICTIONS<br/>
                                    3.1 The Licensee may not, directly or indirectly: (a) sublicense, rent, lease, loan, or otherwise transfer the Software; (b) modify, adapt, translate, or create derivative works based on the Software; (c) reverse engineer, decompile, or disassemble the Software; or (d) remove any proprietary notices or labels on the  Software.<br/>
<br/>
                                    INTELLECTUAL PROPERTY<br/>
                                    4.1 The Software and all intellectual property rights therein are and shall remain the exclusive property of the Licensor. The Licensee acknowledges that it obtains no rights to the Software other than the limited Evaluation License expressly granted in this Agreement.<br/>
<br/>
                                    TERMINATION<br/>
                                    5.1 The Evaluation License shall automatically terminate upon the expiration of the Evaluation Period or upon any breach of this Agreement by the Licensee.<br/>
<br/>
                                    DISCLAIMER OF WARRANTIES<br/>
                                    6.1 The Software is provided "as is" and without any warranty of any kind. Licensor disclaims all warranties, express or implied, including, but not limited to, any implied warranties of merchantability, fitness for a particular purpose, and non-infringement.<br/>
<br/>
                                    LIMITATION OF LIABILITY<br/>
                                    7.1 In no event shall Licensor be liable for any indirect, special, incidental, or consequential damages arising out of the use or inability to use the Software, even if advised of the possibility of such damages.<br/>
<br/>
                                    GOVERNING LAW<br/>
                                    8.1 This Agreement shall be governed by and construed in accordance with the laws of the State of New York, without regard to its conflicts of law principles.<br/>
<br/>
                                    ENTIRE AGREEMENT<br/>
                                    9.1 This Agreement constitutes the entire agreement between the parties concerning the subject matter hereof and supersedes all prior and contemporaneous agreements, understandings, negotiations, and discussions, whether oral or written, between the parties relating thereto.<br/>
<br/>
                                    By accepting this Agreement, the Licensee acknowledges that it has read and understood, and agrees to be bound by, the terms and conditions of this Agreement.<br/>
<br/>
                                    ELA v1.1 April 2023<br/>

                                    <button className="btn text-white bg-cyan-600 hover:bg-cyan-700 sm:w-auto drop-shadow-lg no-underline"
                                       onClick={()=>setTermsModalOpen(false)}>Close</button>
                                </div>
                            </Modal>
                        </div>

                        <div className="mb-4">
                            <a className={`btn font-semibold text-lg text-gray-700 bg-amber-300 hover:bg-amber-200 sm:w-auto drop-shadow-lg no-underline${okToSend ? '' : ' opacity-50 cursor-not-allowed'}`}
                               disabled={!okToSend}
                               onClick={requestTrial}
                               href="#">Setup Trial</a>
                        </div>
                    </div>
                </section>
            }
        </>
    );
}
